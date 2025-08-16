package com.example.orderservice.service;

import com.example.orderservice.client.PaymentClient;
import com.example.orderservice.dto.*;
import com.example.orderservice.client.ItemClient;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.PaymentStatus;
import com.example.orderservice.entity.Status;
import com.example.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final ItemClient itemClient;
    private final PaymentClient paymentClient;

    public OrderService(OrderRepository orderRepository, ItemClient itemClient, PaymentClient paymentClient) {
        this.orderRepository = orderRepository;
        this.itemClient = itemClient;
        this.paymentClient = paymentClient;
    }

    public Order createOrder(OrderRequest request) {
        validateOrderRequest(request);

        BigDecimal totalPrice = BigDecimal.ZERO;
        // Check if the items required in order have enough inventory
        for (Map.Entry<String, Integer> entry : request.getItemQuantities().entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();

            ItemResponse item = itemClient.getItemById(itemId);

            if (item == null) {
                throw new IllegalArgumentException("Item not found: " + itemId);
            }

            if (item.getStock() < quantity) {
                throw new IllegalArgumentException("Insufficient inventory for item: " + itemId);
            }
            totalPrice = totalPrice.add(BigDecimal.valueOf(quantity).multiply(BigDecimal.valueOf(item.getPrice())));
        }

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setUserEmail(request.getUserEmail());
        order.setItemQuantities(request.getItemQuantities());
        order.setTotalAmount(totalPrice.doubleValue());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        // Publish kafka event
        // orderEventPublisher.publishOrderCreated(order);

        try {
            var items = request.getItemQuantities().entrySet().stream()
                    .map(e -> new InventoryAdjustmentRequest.ItemQuantity(e.getKey(), e.getValue()))
                    .toList();
            // Update items to reserve
            itemClient.reserve(new InventoryAdjustmentRequest(order.getId(), items));
            order.setStatus(Status.PENDING);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        } catch (Exception ex) {
            throw new IllegalStateException("Reserve inventory failed", ex);
        }

        return order;
    }

    public Order updateOrder(String id, OrderUpdateRequest orderUpdateRequest) {
        if (id == null) throw new IllegalArgumentException("Order ID must not be null");
        if (orderUpdateRequest.getItemQuantities() == null || orderUpdateRequest.getItemQuantities().isEmpty())
            throw new IllegalArgumentException("At least one item is required");

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        // User can only update pending order
        if (order.getStatus() != Status.PENDING && order.getStatus() != Status.UPDATED) {
            throw new IllegalStateException("Only PENDING/UPDATED orders can be updated");
        }

        // calculate item quantity
        Map<String, Integer> oldQ = order.getItemQuantities();
        // initialize two hashmap to calculate increment/decrement
        Map<String, Integer> inc = new java.util.HashMap<>();
        Map<String, Integer> dec = new java.util.HashMap<>();

        // compare difference with original order item  and quantity
        for (var e : orderUpdateRequest.getItemQuantities().entrySet()) {
            String itemId = e.getKey();
            int newQty = e.getValue();
            int oldQty = oldQ.getOrDefault(itemId, 0);
            int diff = newQty - oldQty;
            if (diff > 0) inc.put(itemId, diff);
            if (diff < 0) dec.put(itemId, -diff);
        }

        for (var e : oldQ.entrySet()) {
            String itemId = e.getKey();
            if (!orderUpdateRequest.getItemQuantities().containsKey(itemId)) {
                dec.put(itemId, e.getValue());
            }
        }

        // Reserve
        // Check the inventory
        for(var e: inc.entrySet()) {
            String itemId = e.getKey();
            int addQty = e.getValue();
            ItemResponse item = itemClient.getItemById(itemId);
            if (item == null) throw new IllegalArgumentException("Item not found: " + itemId);
            if (item.getStock() < addQty) {
                throw new IllegalArgumentException("Insufficient inventory for item: " + itemId + ", need: " + addQty + ", available: " + item.getStock());
            }
        }
        var incItems = inc.entrySet().stream()
                .map(x -> new InventoryAdjustmentRequest.ItemQuantity(x.getKey(), x.getValue()))
                .toList();
        //itemClient.reserve(new InventoryAdjustmentRequest(order.getId(), incItems));

        // Release
        var decItems = dec.entrySet().stream()
                .map(x -> new InventoryAdjustmentRequest.ItemQuantity(x.getKey(), x.getValue()))
                .toList();
        //itemClient.release(new InventoryAdjustmentRequest(order.getId(), decItems));

        boolean reservedInc = false;
        try {
            if (!incItems.isEmpty()) {
                itemClient.reserve(new InventoryAdjustmentRequest(order.getId(), incItems));
                reservedInc = true;
            }

            if (!decItems.isEmpty()) {
                itemClient.release(new InventoryAdjustmentRequest(order.getId(), decItems));
            }
        } catch (Exception ex) {
            // reverse order
            if (reservedInc) {
                try {
                    itemClient.release(new InventoryAdjustmentRequest(order.getId(), incItems));
                } catch (Exception ignore) {
                    System.err.println("Compensation release failed: " + ignore.getMessage());
                }
            }
            throw new IllegalStateException("Update inventory failed: " + ex.getMessage(), ex);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (var e : orderUpdateRequest.getItemQuantities().entrySet()) {
            ItemResponse item = itemClient.getItemById(e.getKey());
            if (item == null) throw new IllegalArgumentException("Item not found: " + e.getKey());
            total = total.add(BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(e.getValue())));
        }

        order.setItemQuantities(orderUpdateRequest.getItemQuantities());
        order.setTotalAmount(total.doubleValue());
        order.setStatus(Status.UPDATED);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order cancelOrder(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID must not be null");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        System.out.println("canceled order: " + order.getId());
        if (Status.CANCELLED.equals(order.getStatus())){
            throw new IllegalStateException("Order already cancelled");
        }

        if(Status.PAID.equals(order.getStatus())) {
            throw new IllegalStateException("Order can not be cancelled directly");
        }

        if(order.getStatus() == Status.PENDING) {
            var items = order.getItemQuantities().entrySet().stream()
                    .map(e -> new InventoryAdjustmentRequest.ItemQuantity(e.getKey(), e.getValue()))
                    .toList();
            var releaseReq = new InventoryAdjustmentRequest(id.toString(), items);
            itemClient.release(releaseReq);
        }
        order.setStatus(Status.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);

        // Publish kafka event
        //orderEventPublisher.publishOrderCancelled(id.toString(), order.getUserEmail());
    }

    public Order getOrder(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID must not be null");
        }

        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    private void validateOrderRequest(OrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request must not be null");
        }

        if (request.getUserEmail() == null || request.getUserEmail().isBlank()) {
            throw new IllegalArgumentException("User email is required");
        }

        if (CollectionUtils.isEmpty(request.getItemQuantities())) {
            throw new IllegalArgumentException("At least one item must be ordered");
        }

    }

    public Order pay(String orderId, String method, String idempotencyKey){
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getStatus() != Status.PENDING && order.getStatus()!=Status.UPDATED)
            throw new IllegalStateException("Order not in payable state");

        PaymentRequest pr = new PaymentRequest();
        pr.setOrderId(order.getId());
        pr.setAmount(BigDecimal.valueOf(order.getTotalAmount()));
        pr.setMethod(method);

        var resp = paymentClient.pay(idempotencyKey, pr);

        switch(resp.getStatus()) {
            case SUCCESS:
                itemClient.commit(new CommitReleaseRequest(orderId));
                order.setStatus(Status.PAID);
                order.setUpdatedAt(LocalDateTime.now());
                return orderRepository.save(order);
            case DUPLICATE:
                return order;
            default:
                // pay failed -> release inventory
                var items = order.getItemQuantities().entrySet().stream()
                        .map(e -> new InventoryAdjustmentRequest.ItemQuantity(e.getKey(), e.getValue()))
                        .toList();
                itemClient.release(new InventoryAdjustmentRequest(orderId, items));

                order.setStatus(Status.PAYMENT_FAILED);
                order.setUpdatedAt(LocalDateTime.now());
                return orderRepository.save(order);
        }
    }

    // Handle event from payment service
    public void handlePaymentEvent(PaymentEvent event) {
        String orderId = event.getOrderId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        System.out.print("ORDER STATUS: " + event.getStatus());

        switch (event.getStatus()) {
            case "PAID":
            case "SUCCESS":
                if (order.getStatus() != Status.PAID) {
                    order.setStatus(Status.PAID);
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);
                    log.info("Order {} status -> PAID (by payment event)", orderId);
                } else {
                    log.info("Order {} already PAID, skip", orderId);
                }
                break;

            case "REFUNDED":
                try {
                    var items = order.getItemQuantities().entrySet().stream()
                            .map(e -> new InventoryAdjustmentRequest.ItemQuantity(e.getKey(), e.getValue()))
                            .toList();
                    itemClient.release(new InventoryAdjustmentRequest(orderId, items));
                } catch (Exception ex) {
                    log.warn("Inventory restock on refund failed for order {}: {}", orderId, ex.getMessage());
                }

                order.setStatus(Status.REFUNDED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                log.info("Order {} status -> REFUNDED (by payment event)", orderId);
                break;

            case "FAILED":
                try {
                    var items = order.getItemQuantities().entrySet().stream()
                            .map(e -> new InventoryAdjustmentRequest.ItemQuantity(e.getKey(), e.getValue()))
                            .toList();
                    itemClient.release(new InventoryAdjustmentRequest(orderId, items));
                } catch (Exception ex) {
                    log.warn("Inventory release on failed payment for order {} failed: {}", orderId, ex.getMessage());
                }

                order.setStatus(Status.PAYMENT_FAILED);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);
                log.info("Order {} status -> PAYMENT_FAILED (by payment event)", orderId);
                break;

            default:
                log.info("Order {} got unknown payment status '{}', skipping", orderId, event.getStatus());
                break;
        }
    }
}

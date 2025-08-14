package com.example.orderservice.service;

import com.example.orderservice.dto.ItemResponse;
import com.example.orderservice.client.ItemClient;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.InventoryAdjustmentRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.Status;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemClient itemClient;

    public OrderService(OrderRepository orderRepository, ItemClient itemClient) {
        this.orderRepository = orderRepository;
        this.itemClient = itemClient;
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
            itemClient.reserve(new InventoryAdjustmentRequest(order.getId().toString(), items));
            order.setStatus(Status.PENDING);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        } catch (Exception ex) {
            throw new IllegalStateException("Reserve inventory failed", ex);
        }

        return order;
    }

//    public Order updateOrder(UUID id, OrderUpdateRequest request) {
//        if (id == null || request == null) {
//            throw new IllegalArgumentException("Order ID and update request must not be null");
//        }
//
//        Order order = orderRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
//
//        if (!CollectionUtils.isEmpty(request.getItemQuantities())) {
//            order.setItemQuantities(request.getItemQuantities());
//        }
//
//        if (request.getTotalAmount() > 0) {
//            order.setTotalAmount(request.getTotalAmount());
//        }
//
//        order.setStatus(Status.UPDATED);
//        order.setUpdatedAt(LocalDateTime.now());
//        return orderRepository.save(order);
//    }
//    public Order pay(UUID orderId, PaymentRequest req) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
//        if (order.getStatus() != Status.PENDING_PAYMENT) {
//            throw new IllegalStateException("Order not in payable state");
//        }
//
//        // 4) 呼叫付款（冪等 key 由前端或你生成）
//        PaymentResponse pr = paymentClient.pay(
//                new PaymentRequest(orderId.toString(), order.getTotalAmount(), req.getMethod(), req.getIdempotencyKey())
//        );
//
//        if ("SUCCESS".equals(pr.getStatus())) {
//            // 5) 轉正庫存（commit）
//            itemClient.commit(Map.of("orderId", orderId.toString()));
//            order.setStatus(Status.PAID);
//        } else {
//            // 6) 釋放庫存（release）
//            itemClient.release(Map.of("orderId", orderId.toString()));
//            order.setStatus(Status.PAYMENT_FAILED);
//        }
//
//        order.setUpdatedAt(LocalDateTime.now());
//        return orderRepository.save(order);
//    }

    public Order cancelOrder(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID must not be null");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        if (Status.CANCELLED.equals(order.getStatus())) {
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

//        if (request.getTotalAmount() <= 0) {
//            throw new IllegalArgumentException("Total amount must be positive");
//        }
    }
}

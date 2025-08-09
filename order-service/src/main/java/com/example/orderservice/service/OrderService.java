package com.example.orderservice.service;

import com.example.orderservice.dto.ItemResponse;
import com.example.orderservice.client.ItemClient;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderUpdateRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.kafka.OrderEventPublisher;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private OrderRepository orderRepository;
    private ItemClient itemClient;
    private OrderEventPublisher orderEventPublisher;

    public Order createOrder(OrderRequest request) {
        validateOrderRequest(request);

        for (Map.Entry<String, Integer> entry : request.getItemQuantities().entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();

            ItemResponse item = itemClient.getItemById(itemId);

            if (item == null) {
                throw new IllegalArgumentException("Item not found: " + itemId);
            }

            if (item.getInventory() < quantity) {
                throw new IllegalArgumentException("Insufficient inventory for item: " + itemId);
            }
        }

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .userEmail(request.getUserEmail())
                .itemQuantities(request.getItemQuantities())
                .totalAmount(request.getTotalAmount())
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Publish kafka event
        orderEventPublisher.publishOrderCreated(order);

        return orderRepository.save(order);
    }

    public Order updateOrder(UUID id, OrderUpdateRequest request) {
        if (id == null || request == null) {
            throw new IllegalArgumentException("Order ID and update request must not be null");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        if (!CollectionUtils.isEmpty(request.getItemQuantities())) {
            order.setItemQuantities(request.getItemQuantities());
        }

        if (request.getTotalAmount() > 0) {
            order.setTotalAmount(request.getTotalAmount());
        }

        order.setStatus("UPDATED");
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public void cancelOrder(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Order ID must not be null");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new IllegalStateException("Order already cancelled");
        }

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Publish kafka event
        orderEventPublisher.publishOrderCancelled(id.toString(), order.getUserEmail());
    }

    public Order getOrder(UUID id) {
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

        if (request.getTotalAmount() <= 0) {
            throw new IllegalArgumentException("Total amount must be positive");
        }
    }
}

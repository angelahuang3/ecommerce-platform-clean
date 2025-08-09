package com.example.orderservice.kafka;

import com.example.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCancelled(String orderId, String userEmail) {
        Map<String, Object> event = Map.of(
                "orderId", orderId,
                "userEmail", userEmail,
                "action", "CANCEL_PAYMENT"
        );

        kafkaTemplate.send("order-events", event);
    }

    public void publishOrderCreated(Order order) {
        Map<String, Object> event = Map.of(
                "orderId", order.getId().toString(),
                "itemQuantities", order.getItemQuantities(),
                "action", "ORDER_CREATED"
        );

        kafkaTemplate.send("order-events", event);
    }

}
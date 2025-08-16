package com.example.orderservice.kafka;

import com.example.orderservice.dto.PaymentEvent;
import com.example.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
    private static final String TOPIC = "payment-events";

    private final OrderService orderService;
    public PaymentEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics=TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consume(PaymentEvent paymentEvent) {
        log.info("[Order] received payment event: orderId={}, paymentId={}, status={}",
                paymentEvent.getOrderId(), paymentEvent.getPaymentId(), paymentEvent.getStatus());
        orderService.handlePaymentEvent(paymentEvent);
    }
}

package com.example.paymentservice.kafka;

import com.example.paymentservice.dto.PaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaymentEventProducer {
    public static final String TOPIC = "payment-events";
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventProducer.class);

    public PaymentEventProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentEvent(PaymentEvent paymentEvent) {
        kafkaTemplate.send(TOPIC, paymentEvent.getOrderId(), paymentEvent);
        logger.info("[Payment] Sent event: order={}, payment={}, status={}",
                paymentEvent.getOrderId(), paymentEvent.getPaymentId(), paymentEvent.getStatus());
    }
}

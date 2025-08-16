// service/PaymentService.java
package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentEvent;
import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentStatus;
import com.example.paymentservice.kafka.PaymentEventProducer;
import com.example.paymentservice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository repo;
    private final PaymentEventProducer producer;
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    public PaymentService(PaymentRepository repo, PaymentEventProducer producer) {
        this.repo = repo;
        this.producer = producer;
    }

    @Transactional
    public PaymentResponse pay(String idemKey, PaymentRequest req) {
        // Check for Idempotency
        Optional<Payment> existing = repo.findByIdempotencyKey(idemKey);
        if (existing.isPresent()) {
            Payment p = existing.get();
            return toResponse(p, PaymentStatus.DUPLICATE);
        }

        // mock as provider
        boolean ok = req.getAmount() != null && req.getAmount().signum() > 0;

        Payment p = new Payment();
        p.setOrderId(req.getOrderId());
        p.setAmount(req.getAmount());
        p.setMethod(req.getMethod());
        p.setIdempotencyKey(idemKey);
        p.setStatus(ok ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());

        // Prevent race condition when two requests with the same idempotency key arrive at the same time.
        try {
            repo.save(p);
        } catch (DataIntegrityViolationException e) {
            Payment dup = repo.findByIdempotencyKey(idemKey).orElseThrow(() -> e);
            return toResponse(dup, PaymentStatus.DUPLICATE);
        }

        if (p.getStatus() == PaymentStatus.SUCCESS) {
            producer.sendPaymentEvent(new PaymentEvent(p.getOrderId(), p.getId(), "PAID"));
            log.info("Pay success -> event sent: orderId={}, paymentId={}", p.getOrderId(), p.getId());
        }

        return toResponse(p, p.getStatus());
    }

    @Transactional
    public PaymentResponse refund(String idemKey, String paymentId) {
        // Check for Idempotency
        Optional<Payment> existing = repo.findByIdempotencyKey(idemKey);
        if (existing.isPresent()) {
            Payment p = existing.get();
            return toResponse(p, PaymentStatus.DUPLICATE);
        }

        Payment p = repo.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (p.getStatus() == PaymentStatus.REFUNDED) {
            PaymentResponse r = new PaymentResponse();
            r.setPaymentId(p.getId());
            r.setStatus(PaymentStatus.REFUNDED);
            return r;
        }

        p.setStatus(PaymentStatus.REFUNDED);
        repo.save(p);

        producer.sendPaymentEvent(new PaymentEvent(p.getOrderId(), p.getId(), "REFUNDED"));
        log.info("Refund success -> event sent: orderId={}, paymentId={}", p.getOrderId(), p.getId());

        PaymentResponse r = new PaymentResponse();
        r.setPaymentId(p.getId());
        r.setOrderId(p.getOrderId());
        r.setStatus(PaymentStatus.REFUNDED);
        return r;
    }

    public PaymentResponse getByOrderId(String orderId){
        Payment p = repo.findTopByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        return toResponse(p, p.getStatus());
    }

    public PaymentResponse toResponse(Payment p, PaymentStatus status) {
        PaymentResponse resp = new PaymentResponse();
        resp.setPaymentId(p.getId());
        resp.setOrderId(p.getOrderId());
        resp.setStatus(status);
        return resp;
    }
}

// service/PaymentService.java
package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository repo;
    public PaymentService(PaymentRepository repo){
        this.repo = repo;
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
        repo.save(p);

        return toResponse(p, p.getStatus());
    }

    @Transactional
    public PaymentResponse refund(String paymentId) {
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
        PaymentResponse r = new PaymentResponse();
        r.setPaymentId(p.getId());
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

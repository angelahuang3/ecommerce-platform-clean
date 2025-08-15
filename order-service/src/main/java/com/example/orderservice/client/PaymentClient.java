package com.example.orderservice.client;

import com.example.orderservice.dto.PaymentRequest;
import com.example.orderservice.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="payment-service", url = "${payment.service.url:http://payment-service:8084}", configuration = com.example.orderservice.config.FeignAuthConfig.class)
public interface PaymentClient {
    @PostMapping("/api/payments")
    PaymentResponse pay(@RequestHeader("Idempotency-Key") String idemKey, @RequestBody PaymentRequest request);

    @GetMapping("/api/payments/{orderId}")
    PaymentResponse getByOrderId(@PathVariable String orderId);

    @PostMapping("/api/payments/{id}/refund")
    PaymentResponse refund(@PathVariable("id") String paymentId);
}

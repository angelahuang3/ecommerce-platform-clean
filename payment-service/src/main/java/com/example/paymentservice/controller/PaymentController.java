package com.example.paymentservice.controller;

import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService){
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(@RequestHeader("Idempotency-Key") String idemKey, @RequestBody PaymentRequest req) {
        return ResponseEntity.ok(paymentService.pay(idemKey, req));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refund(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.refund(id));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getByOrderId(orderId));
    }

}

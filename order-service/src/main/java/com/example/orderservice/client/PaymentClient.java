package com.example.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name="payment-service", url = "${PAYMENT_SERVICE_URL}")
public interface PaymentClient {
//    @PostMapping("/api/payments")
//    PaymentResponse pay(@RequestBody PaymentRequest request);
}

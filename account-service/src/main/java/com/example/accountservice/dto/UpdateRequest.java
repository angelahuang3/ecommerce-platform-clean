package com.example.accountservice.dto;
import lombok.Data;

@Data
public class UpdateRequest {
    private String username;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
}

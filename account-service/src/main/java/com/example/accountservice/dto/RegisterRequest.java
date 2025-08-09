package com.example.accountservice.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
}

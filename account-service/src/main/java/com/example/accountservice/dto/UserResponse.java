package com.example.accountservice.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String billingAddress;
    private String shippingAddress;
    private String paymentMethod;
}

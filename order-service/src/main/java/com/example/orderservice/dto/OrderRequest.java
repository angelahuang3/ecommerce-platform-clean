package com.example.orderservice.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OrderRequest {
    private String userEmail;
    private Map<String, Integer> itemQuantities; // itemId -> quantity
    private double totalAmount;
}

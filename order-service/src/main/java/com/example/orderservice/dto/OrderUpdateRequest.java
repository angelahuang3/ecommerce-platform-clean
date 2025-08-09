package com.example.orderservice.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OrderUpdateRequest {
    private Map<String, Integer> itemQuantities; // itemId -> quantity
    private double totalAmount;
    private String status;
}

package com.example.orderservice.dto;
import java.util.List;
import java.util.Map;

public class OrderUpdateRequest {
    private Map<String, Integer> itemQuantities; // itemId -> quantity
    private double totalAmount;
    private String status;

    public Map<String, Integer> getItemQuantities() {
        return itemQuantities;
    }

    public void setItemQuantities(Map<String, Integer> itemQuantities) {
        this.itemQuantities = itemQuantities;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

package com.example.itemservice.dto;
import java.util.Map;

public class ReserveRequest {
    private String orderId;
    private Map<String, Integer> items;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public void setItems(Map<String, Integer> items) {
        this.items = items;
    }
}
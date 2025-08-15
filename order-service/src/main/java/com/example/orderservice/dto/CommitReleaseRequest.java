package com.example.orderservice.dto;

public class CommitReleaseRequest {
    private String orderId;

    public CommitReleaseRequest(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}

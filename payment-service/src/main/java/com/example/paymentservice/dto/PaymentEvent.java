package com.example.paymentservice.dto;

import java.io.Serializable;

public class PaymentEvent implements Serializable {
    private String orderId;
    private String paymentId;
    private String status; // "PAID" or "REFUNDED"

    public PaymentEvent() {}
    public PaymentEvent(String orderId, String paymentId, String status) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
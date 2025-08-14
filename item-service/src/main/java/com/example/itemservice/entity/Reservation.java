package com.example.itemservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "reservations")
public class Reservation {
    @Id
    private String orderId;
    private Map<String, Integer> itemQuantities; // itemId -> qty
    private String status;               // RESERVED / COMMITTED / RELEASED

    public Reservation() {}
    public Reservation(String orderId, Map<String, Integer> itemQuantities, String status) {
        this.orderId = orderId;
        this.itemQuantities = itemQuantities;
        this.status = status;
    }
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Map<String, Integer> getItemQuantities() {
        return itemQuantities;
    }

    public void setItemQuantities(Map<String, Integer> itemQuantities) {
        this.itemQuantities = itemQuantities;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

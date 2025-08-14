package com.example.orderservice.dto;
import java.util.List;
import java.util.Map;

public class InventoryAdjustmentRequest {
    private String orderId;
    private List<ItemQuantity> items;

    public static class ItemQuantity {
        private String itemId;
        private int quantity;
        public ItemQuantity() {}
        public ItemQuantity(String itemId, int quantity) {
            this.itemId = itemId; this.quantity = quantity;
        }
        public String getItemId() { return itemId; }
        public int getQuantity() { return quantity; }
        public void setItemId(String itemId) { this.itemId = itemId; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public InventoryAdjustmentRequest() {}
    public InventoryAdjustmentRequest(String orderId, List<ItemQuantity> items) {
        this.orderId = orderId; this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<ItemQuantity> getItems() {
        return items;
    }

    public void setItems(List<ItemQuantity> items) {
        this.items = items;
    }
}
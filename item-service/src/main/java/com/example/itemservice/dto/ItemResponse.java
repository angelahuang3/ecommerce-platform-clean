package com.example.itemservice.dto;

public class ItemResponse {
    private String id;
    private String name;
    private double price;
    private String upc;
    private int stock;
    private int reserved;
    private String imgUrl;

    public ItemResponse(String id, String name, double price, String upc, int stock, int reserved, String imgUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.upc = upc;
        this.stock = stock;
        this.reserved = reserved;
        this.imgUrl = imgUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(String upc) {
        this.upc = upc;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}

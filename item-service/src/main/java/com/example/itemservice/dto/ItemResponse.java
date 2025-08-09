package com.example.itemservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemResponse {
    private String id;
    private String name;
    private double price;
    private String upc;
    private int inventory;
    private String imgUrl;
}

package com.example.orderservice.entity;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@Table("orders")
public class Order {
    @PrimaryKey
    private UUID id;

    private String userEmail;

    @Column("item_quantities")
    private Map<String, Integer> itemQuantities;

    private double totalAmount;
    private String status;  // Created, Canceled, Paid, Completed
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

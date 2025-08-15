package com.example.orderservice.client;

import com.example.orderservice.dto.CommitReleaseRequest;
import com.example.orderservice.dto.ItemResponse;
import com.example.orderservice.dto.InventoryAdjustmentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "item-service", url = "${item.service.url:http://item-service:8082}", configuration = com.example.orderservice.config.FeignAuthConfig.class)
public interface ItemClient {
    @PostMapping("/api/items/inventory/reserve")
    void reserve(@RequestBody InventoryAdjustmentRequest req);

    @PostMapping("/api/items/inventory/commit")
    void commit(@RequestBody CommitReleaseRequest req);

    @PostMapping("/api/items/inventory/release")
    void release(@RequestBody InventoryAdjustmentRequest req);

    @GetMapping("/api/items/{id}")
    ItemResponse getItemById(@PathVariable("id") String itemId);
}

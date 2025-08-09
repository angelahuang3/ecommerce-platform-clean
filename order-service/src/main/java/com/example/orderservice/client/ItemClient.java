package com.example.orderservice.client;

import com.example.orderservice.dto.ItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "item-service", url = "${ITEM_SERVICE_URL:localhost:8082}")
public interface ItemClient {
    @GetMapping
    ItemResponse getItemById(@PathVariable("id") String itemId);
}

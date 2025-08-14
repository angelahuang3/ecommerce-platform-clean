package com.example.itemservice.service;

import com.example.itemservice.entity.Item;
import com.example.itemservice.repository.ItemRepository;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Map;

public class ItemService {
    private ItemRepository itemRepository;
    @KafkaListener(topics = "order-events", groupId = "item-group")
    public void handleOrderCreated(Map<String, Object> message) {
        if(!"ORDER_CREATED".equals(message.get("action"))) {
            return;
        }
        Map<String, Integer> itemQuantities = (Map<String, Integer>) message.get("itemQuantities");
        itemQuantities.forEach((itemId, qty) ->
        {
           Item item = itemRepository.findById(itemId)
                   .orElseThrow(() -> new RuntimeException("item not found"));

           if(item.getStock() < qty){
               throw new  RuntimeException("insufficient inventory");
           }
           item.setStock(item.getStock() - qty);
           item.setStock(item.getStock() - qty);
           itemRepository.save(item);
        });
    }

}

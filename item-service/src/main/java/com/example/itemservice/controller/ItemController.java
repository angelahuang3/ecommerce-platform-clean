package com.example.itemservice.controller;

import com.example.itemservice.dto.ItemResponse;
import com.example.itemservice.entity.Item;
import com.example.itemservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    @Autowired
    private ItemRepository itemRepository;

    @GetMapping
    public List<Item> getAll(){
        return itemRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable("id") String itemId) {
        return itemRepository.findById(itemId)
                .map(item -> ResponseEntity.ok(
                        ItemResponse.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .price(item.getPrice())
                                .upc(item.getUpc())
                                .inventory(item.getInventory())
                                .imgUrl(item.getImgUrl())
                                .build()
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> addItem(@RequestBody Item item){
        if (itemRepository.findByUpc(item.getUpc()).isPresent()) {
            return ResponseEntity.status(409).body(
                    Map.of("error", "Item with same UPC already exists")
            );
        }

        Item saved = itemRepository.save(item);
        return ResponseEntity.ok(Map.of(
                "message", "Item added successfully",
                "item", saved
        ));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateItem(@PathVariable String id, @RequestBody Item updatedItem){
        return itemRepository.findById(id)
                        .map(item -> {
            item.setName(updatedItem.getName());
            item.setPrice(updatedItem.getPrice());
            item.setUpc(updatedItem.getUpc());
            item.setInventory(updatedItem.getInventory());
            item.setImgUrl(updatedItem.getImgUrl());
            Item saved = itemRepository.save(item);
            return ResponseEntity.ok(Map.of(
                    "message", "Item updated",
                    "item", saved
            ));
        })
        .orElseGet(() -> ResponseEntity.status(404).body(
                    Map.of("error", "Item not found")
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteItem(@PathVariable String id){
        if(itemRepository.findById(id).isPresent()){
            itemRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Item deleted"));
        }else {
            return ResponseEntity.status(404).body(Map.of("error", "Item not found"));
        }
    }
}

package com.example.itemservice.controller;

import com.example.itemservice.dto.CommitReleaseRequest;
import com.example.itemservice.dto.InventoryAdjustmentRequest;
import com.example.itemservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> reserve(@RequestBody InventoryAdjustmentRequest req) {
        inventoryService.reserve(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/commit")
    public ResponseEntity<?> commit(@RequestBody CommitReleaseRequest req) {
        inventoryService.commit(req);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/release")
    public ResponseEntity<?> release(@RequestBody InventoryAdjustmentRequest req) {
        inventoryService.release(req);
        return ResponseEntity.ok().build();
    }
}

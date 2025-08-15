package com.example.itemservice.service;

import com.example.itemservice.dto.CommitReleaseRequest;
import com.example.itemservice.dto.InventoryAdjustmentRequest;
import com.example.itemservice.entity.Item;
import com.example.itemservice.entity.Reservation;
import com.example.itemservice.repository.ItemRepository;
import com.example.itemservice.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final ItemRepository itemRepository;
    private final ReservationRepository reservationRepository;

    public InventoryService(ItemRepository itemRepository, ReservationRepository reservationRepository) {
        this.itemRepository = itemRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void reserve(InventoryAdjustmentRequest req) {
        // Idempotency - return directly for the item already reserved
        if (reservationRepository.existsById(req.getOrderId())) return;

        // Verify the item
        for (var it : req.getItems()) {
            Item item = itemRepository.findById(it.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + it.getItemId()));
            if (item.getStock() < it.getQuantity()) {
                throw new IllegalStateException("Insufficient inventory for item: " + it.getItemId());
            }
        }
        // Update the stock and reserved amount
        for (var it : req.getItems()) {
            Item item = itemRepository.findById(it.getItemId()).get();
            item.setStock(item.getStock() - it.getQuantity());
            item.setReserved(item.getReserved() + it.getQuantity());
            itemRepository.save(item);
        }
        // Record reservation
        Map<String,Integer> map = req.getItems().stream()
                .collect(Collectors.toMap(InventoryAdjustmentRequest.ItemQuantity::getItemId,
                        InventoryAdjustmentRequest.ItemQuantity::getQuantity));
        reservationRepository.save(new Reservation(req.getOrderId(), map, "RESERVED"));
    }

    @Transactional
    public void commit(CommitReleaseRequest req) {
        Reservation reservation = reservationRepository.findById(req.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + req.getOrderId()));
        if("COMMITTED".equals(reservation.getStatus())) return; //Idempotency

        for(var e: reservation.getItemQuantities().entrySet()){
            Item it =  itemRepository.findById(e.getKey()).orElseThrow(() -> new IllegalArgumentException("Item not found: " + e.getKey()));
            it.setReserved(it.getReserved() - e.getValue());
            itemRepository.save(it);
        }
        reservation.setStatus("COMMITTED");
        reservationRepository.save(reservation);
    }

    @Transactional
    public void release(InventoryAdjustmentRequest req) {
        Reservation r = reservationRepository.findById(req.getOrderId()).orElse(null);
        if (r == null || "RELEASED".equals(r.getStatus())) return; // return the order that already released

        for (var entry : r.getItemQuantities().entrySet()) {
            Item item = itemRepository.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + entry.getKey()));
            item.setStock(item.getStock() + entry.getValue());
            item.setReserved(item.getReserved() - entry.getValue());
            itemRepository.save(item);
        }

        Map<String,Integer> map = req.getItems().stream()
                .collect(Collectors.toMap(InventoryAdjustmentRequest.ItemQuantity::getItemId,
                        InventoryAdjustmentRequest.ItemQuantity::getQuantity));
        reservationRepository.save(new Reservation(req.getOrderId(), map, "RELEASED"));
    }
}
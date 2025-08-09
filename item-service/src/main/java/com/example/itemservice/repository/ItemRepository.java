package com.example.itemservice.repository;

import com.example.itemservice.entity.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ItemRepository extends MongoRepository<Item, String> {
    Optional<Object> findByUpc(String upc);
}

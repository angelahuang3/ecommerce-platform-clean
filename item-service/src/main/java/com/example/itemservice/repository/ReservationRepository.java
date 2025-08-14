package com.example.itemservice.repository;

import com.example.itemservice.entity.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservationRepository  extends MongoRepository<Reservation, String> {

}
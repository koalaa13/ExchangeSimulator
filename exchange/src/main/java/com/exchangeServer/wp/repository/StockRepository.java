package com.exchangeServer.wp.repository;

import com.exchangeServer.wp.domain.Stock;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends MongoRepository<Stock, String> {
    List<Stock> findByName(String name);
}

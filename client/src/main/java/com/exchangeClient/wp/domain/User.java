package com.exchangeClient.wp.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.HashMap;
import java.util.Map;

@Data
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;
    private int balance;
    private Map<String, Integer> stocks; // name -> count

    public User(String name) {
        this.name = name;
        this.balance = 0;
        this.stocks = new HashMap<>();
    }

    public void changeStockCount(final String name, final int count) {
        int currentCount = stocks.getOrDefault(name, 0);
        int newCount = currentCount + count;
        assert newCount >= 0;
        if (newCount == 0) {
            stocks.remove(name);
        } else {
            stocks.put(name, newCount);
        }
    }

    public void changeBalance(final int cost) {
        assert balance + cost >= 0;
        balance += cost;
    }
}

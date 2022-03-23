package com.exchangeServer.wp.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Stock {
    @Id
    private String id;

    private final String name;
    private int count;
    private int price;

    public Stock(String name, int count, int price) {
        this.name = name;
        this.count = count;
        this.price = price;
    }

    public void changeCount(int count) {
        assert this.count + count >= 0;
        this.count += count;
    }
}

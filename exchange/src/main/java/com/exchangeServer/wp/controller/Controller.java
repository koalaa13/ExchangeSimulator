package com.exchangeServer.wp.controller;

import com.exchangeServer.wp.domain.Stock;
import com.exchangeServer.wp.service.StockService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Controller {
    private final StockService stockService;

    public Controller(StockService stockService) {
        this.stockService = stockService;
    }

    @RequestMapping("/ipo")
    public String addStock(@RequestParam final String name, @RequestParam final int count,
                           @RequestParam final int price) {
        Stock stock;
        try {
            stock = stockService.ipo(name, count, price);
        } catch (Exception e) {
            return "Error happened";
        }
        return "Successfully added stock: " + stock;
    }

    @RequestMapping("/all")
    public List<Stock> getAll() {
        return stockService.findAll();
    }

    @RequestMapping("/change_price")
    public String changePrice(@RequestParam final String name, @RequestParam final int newPrice) {
        return "Changed price of " + stockService.updatePrice(name, newPrice) + " stocks";
    }

    @RequestMapping("/get_price_by_name")
    public int getPriceByName(@RequestParam final String name) {
        return stockService.getPriceByName(name);
    }

    @RequestMapping("/get_by_name")
    public List<Stock> getByName(@RequestParam final String name) {
        return stockService.findAllByName(name);
    }

    @RequestMapping("/buy")
    public int buy(@RequestParam final String name, @RequestParam final int count, @RequestParam final int cost) {
        return stockService.buy(name, count, cost);
    }

    @RequestMapping("/sell")
    public int sell(@RequestParam final String name, @RequestParam final int count, @RequestParam final int cost) {
        return stockService.sell(name, count, cost);
    }
}

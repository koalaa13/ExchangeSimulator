package com.exchangeServer.wp.service;

import com.exchangeServer.wp.domain.Stock;
import com.exchangeServer.wp.repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StockService {
    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public Stock ipo(String name, int count, int price) {
        return stockRepository.save(new Stock(name, count, price));
    }

    public List<Stock> findAll() {
        return stockRepository.findAll();
    }

    public List<Stock> findAllByName(String name) {
        return stockRepository.findByName(name);
    }

    public Optional<Stock> findOneByName(String name) {
        List<Stock> all = findAllByName(name);
        if (all.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(all.get(0));
        }
    }

    public int getPriceByName(String name) {
        var optionalStock = findOneByName(name);
        if (optionalStock.isPresent()) {
            Stock stock = optionalStock.get();
            return stock.getPrice();
        } else {
            return 0;
        }
    }

    public int updatePrice(String name, int newPrice) {
        List<Stock> stocks = stockRepository.findByName(name);
        stocks.forEach(stock -> stock.setPrice(newPrice));
        stockRepository.saveAll(stocks);
        return stocks.size();
    }

    public void updateCount(String name, int countChange) {
        List<Stock> stocks = stockRepository.findByName(name);
        stocks.forEach(stock -> stock.changeCount(countChange));
        stockRepository.saveAll(stocks);
    }

    public int buy(String name, int count, int cost) {
        var optionalStock = findOneByName(name);
        if (optionalStock.isPresent()) {
            Stock stock = optionalStock.get();
            if (stock.getPrice() <= cost) {
                int bought = Math.min(stock.getCount(), count);
                updateCount(name, -count);
                return bought;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public void clear() {
        stockRepository.deleteAll();
    }

    public int sell(String name, int count, int cost) {
        var optionalStock = findOneByName(name);
        if (optionalStock.isPresent()) {
            Stock stock = optionalStock.get();
            if (stock.getPrice() >= cost) {
                updateCount(name, count);
                return count;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
}

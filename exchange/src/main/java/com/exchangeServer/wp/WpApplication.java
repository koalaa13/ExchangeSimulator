package com.exchangeServer.wp;

import com.exchangeServer.wp.domain.Stock;
import com.exchangeServer.wp.service.StockService;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class WpApplication {
    private static class MyRandom extends Random {
        public MyRandom(int seed) {
            super(seed);
        }

        public int nextNonNegative(int bound) {
            return next(Integer.SIZE - 1) % bound;
        }
    }

    public static void main(String[] args) {
        var applicationContext = SpringApplication.run(WpApplication.class, args);
//        StockService stockService = (StockService) applicationContext.getBean("stockService");
//        MyRandom random = new MyRandom(1337228);

//        stockService.clear();
//        stockService.ipo("Yandex", 100, 100);
//        stockService.ipo("Google", 1000, 1000);
//        stockService.ipo("Meta", 150, 300);
//        stockService.ipo("Amazon", 1100, 2000);
//        List<String> companies = stockService.findAll()
//                .stream().map(Stock::getName)
//                .collect(Collectors.toList());

//        Runnable priceChanging = new Runnable() {
//            @SneakyThrows
//            @Override
//            public void run() {
//                List<Stock> companies = stockService.findAll();
//                int companyId = random.nextNonNegative(companies.size());
//                int newPrice = random.nextNonNegative(3000);
//                stockService.updatePrice(companies.get(companyId).getName(), newPrice);
//            }
//        };

        // randomly change cost of random stock every 5 seconds
//        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
//        executorService.scheduleAtFixedRate(priceChanging, 0, 5, TimeUnit.SECONDS);
    }
}

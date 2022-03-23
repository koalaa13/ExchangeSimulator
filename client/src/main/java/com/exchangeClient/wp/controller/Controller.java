package com.exchangeClient.wp.controller;

import com.exchangeClient.wp.domain.User;
import com.exchangeClient.wp.service.ExchangeService;
import com.exchangeClient.wp.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class Controller {
    private final UserService userService;
    private final ExchangeService exchangeService;

    public Controller(UserService userService, ExchangeService exchangeService) {
        this.userService = userService;
        this.exchangeService = exchangeService;
    }

    @RequestMapping("/register")
    public String register(@RequestParam final String username) {
        User user;
        try {
            user = userService.register(username);
        } catch (Exception e) {
            return "Error happened";
        }
        return "Successfully added user " + user;
    }

    @RequestMapping("/deposit")
    public String deposit(@RequestParam final String name, @RequestParam final int amount) {
        return "Added " + amount + "money for " + userService.updateBalance(name, amount) + " users.";
    }

    private Map<String, Integer> getUsersStocksPrices(User user) {
        Map<String, Integer> res = new HashMap<>();
        for (var stock : user.getStocks().entrySet()) {
            int stockPrice = exchangeService.getStockPrice(stock.getKey());
            res.put(stock.getKey(), stockPrice);
        }
        return res;
    }

    @RequestMapping("/user_profile")
    public String getUsersStocks(@RequestParam final String username) {
        var optionalUser = userService.findOneByName(username);
        if (optionalUser.isPresent()) {
            StringBuilder res = new StringBuilder();
            User user = optionalUser.get();
            Map<String, Integer> prices = getUsersStocksPrices(user);
            prices.forEach((name, price) ->
                    res.append("Stock: ").append(name)
                            .append(" count: ").append(user.getStocks().get(name))
                            .append(" price: ").append(price).append('\n')
            );
            return res.toString();
        } else {
            return "No such user";
        }
    }

    @RequestMapping("/user_stocks_value")
    public int getUserStocksValue(@RequestParam final String username) {
        var optionalUser = userService.findOneByName(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Map<String, Integer> prices = getUsersStocksPrices(user);
            final int[] sum = {0};
            prices.forEach((name, price) -> sum[0] += price);
            return sum[0];
        } else {
            return 0;
        }
    }

    @RequestMapping("/buy_stock")
    public int buyStock(@RequestParam final String username, @RequestParam final String stockName,
                        @RequestParam final int count, @RequestParam final int price) {
        var optionalUser = userService.findOneByName(username);
        if (optionalUser.isPresent()) {
            int bought = exchangeService.buyStock(stockName, count, price);
            userService.updateStockCount(username, stockName, bought);
            userService.updateBalance(username, -count * price);
            return bought;
        } else {
            return 0;
        }
    }

    @RequestMapping("/sell_stock")
    public int sellStock(@RequestParam final String username, @RequestParam final String stockName,
                        @RequestParam final int count, @RequestParam final int price) {
        var optionalUser = userService.findOneByName(username);
        if (optionalUser.isPresent()) {
            int sold = exchangeService.sellStock(stockName, count, price);
            userService.updateStockCount(username, stockName, -sold);
            userService.updateBalance(username, count * price);
            return sold;
        } else {
            return 0;
        }
    }
}

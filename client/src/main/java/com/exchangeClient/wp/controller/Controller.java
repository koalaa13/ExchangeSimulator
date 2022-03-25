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

    @RequestMapping("/clear")
    public void clear() {
        userService.clear();
    }

    @RequestMapping("/register")
    public String register(@RequestParam final String username) {
        if (userService.findOneByName(username).isPresent()) {
            return "Such user already exists";
        }
        try {
            userService.register(username);
        } catch (Exception e) {
            return "Error happened";
        }
        return "Successfully added user " + username;
    }

    @RequestMapping("/deposit")
    public String deposit(@RequestParam final String name, @RequestParam final int amount) {
        return "Added " + amount + " money for " + userService.updateBalance(name, amount) + " users.";
    }

    private Map<String, Integer> getUsersStocksPrices(User user) {
        Map<String, Integer> res = new HashMap<>();
        for (var stock : user.getStocks().entrySet()) {
            int stockPrice = exchangeService.getStockPrice(stock.getKey());
            res.put(stock.getKey(), stockPrice);
        }
        return res;
    }

    @RequestMapping("/balance")
    public int getBalance(@RequestParam final String username) {
        var optionalUser = userService.findOneByName(username);
        return optionalUser.map(User::getBalance).orElse(0);
    }

    @RequestMapping("/user_stocks")
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

    @RequestMapping("/user_profile")
    public String getUser(@RequestParam final String username) {
        var optionalUser = userService.findOneByName(username);
        if (optionalUser.isPresent()) {
            return optionalUser.get().toString();
        } else {
            return "No such user with name " + username;
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
            User user = optionalUser.get();
            if (user.getBalance() < price * count) {
                return 0;
            }
            int bought = exchangeService.buyStock(stockName, count, price);
            userService.updateStockCount(username, stockName, bought);
            userService.updateBalance(username, -bought * price);
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
            User user = optionalUser.get();
            if (user.getStocks().getOrDefault(stockName, 0) < count) {
                return 0;
            }
            int sold = exchangeService.sellStock(stockName, count, price);
            userService.updateStockCount(username, stockName, -sold);
            userService.updateBalance(username, sold * price);
            return sold;
        } else {
            return 0;
        }
    }
}

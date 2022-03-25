package com.exchangeClient.wp;

import com.exchangeClient.wp.service.UserService;
import org.apache.commons.compress.archivers.sevenz.CLI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.*;
import java.net.URL;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
public class ClientTest {
    private static final int EXCHANGE_PORT = 8080;
    private static final int CLIENT_PORT = 8090;
    private static ConfigurableApplicationContext clientContext;

    private static final Network network = Network.newNetwork();

    private String readFromUrl(final String url) {
        System.out.println("URL: " + url);
        try {
            try (InputStream is = new URL(url).openConnection().getInputStream()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    return reader.lines().collect(Collectors.joining());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String sendRequest(final int port, final String func, final String params) {
        String URI = "http://localhost:" + port + "/" + func;
        if (!"".equals(params)) {
            URI += "?" + params;
        }
        return readFromUrl(URI);
    }


    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:latest")
            .withNetwork(network)
            .withNetworkAliases("mongo");

    @Container
    private static final FixedHostPortGenericContainer<?> EXCHANGE =
            new FixedHostPortGenericContainer<>("exchange:1.0-SNAPSHOT")
                    .withFixedExposedPort(EXCHANGE_PORT, EXCHANGE_PORT)
                    .withExposedPorts(EXCHANGE_PORT)
                    .withNetwork(network);

    @BeforeAll
    public static void beforeAll() {
        clientContext = SpringApplication.run(WpApplication.class);

        EXCHANGE.start();
        MONGO_DB_CONTAINER.start();
        assert EXCHANGE.isRunning();
        assert MONGO_DB_CONTAINER.isRunning();
        System.out.println(EXCHANGE.getHost());
        System.out.println(EXCHANGE.getFirstMappedPort());
        System.out.println(MONGO_DB_CONTAINER.getHost());
        System.out.println(MONGO_DB_CONTAINER.getFirstMappedPort());
    }

    @BeforeEach
    public void beforeEach() {
        sendRequest(CLIENT_PORT, "clear", "");
        sendRequest(EXCHANGE_PORT, "clear", "");

        sendRequest(EXCHANGE_PORT, "ipo", "name=Yandex&count=100&price=100");
        sendRequest(EXCHANGE_PORT, "ipo", "name=Google&count=1000&price=1000");
        sendRequest(EXCHANGE_PORT, "ipo", "name=Meta&count=150&price=300");
        sendRequest(EXCHANGE_PORT, "ipo", "name=Amazon&count=1100&price=2000");
    }

    @AfterAll
    public static void afterAll() {
        network.close();

        clientContext.close();
    }

    private int getCountByStockName(final String stockName) {
        return Integer.parseInt(sendRequest(EXCHANGE_PORT, "get_count_by_name", "name=" + stockName));
    }

    private int getPriceByStockName(final String stockName) {
        return Integer.parseInt(sendRequest(EXCHANGE_PORT, "get_price_by_name", "name=" + stockName));
    }

    private String changePrice(final String stockName, final int newPrice) {
        return sendRequest(EXCHANGE_PORT, "change_price", "name=" + stockName + "&newPrice=" + newPrice);
    }

    private String deposit(final String username, final int amount) {
        return sendRequest(CLIENT_PORT, "deposit", "name=" + username + "&amount=" + amount);
    }

    private String register(final String username) {
        return sendRequest(CLIENT_PORT, "register", "username=" + username);
    }

    private int getBalance(final String username) {
        return Integer.parseInt(sendRequest(CLIENT_PORT, "balance", "username=" + username));
    }

    private String userStocks(final String username) {
        return sendRequest(CLIENT_PORT, "user_stocks", "username=" + username);
    }

    private String userProfile(final String username) {
        return sendRequest(CLIENT_PORT, "user_profile", "username=" + username);
    }

    private int userStocksValue(final String username) {
        return Integer.parseInt(sendRequest(CLIENT_PORT, "user_stocks_value", "username=" + username));
    }

    private int buyStock(final String username, final String stockName, final int count, final int price) {
        return Integer.parseInt(sendRequest(CLIENT_PORT, "buy_stock",
                "username=" + username + "&stockName=" + stockName + "&count=" + count + "&price=" + price));
    }

    private int sellStock(final String username, final String stockName, final int count, final int price) {
        return Integer.parseInt(sendRequest(CLIENT_PORT, "sell_stock",
                "username=" + username + "&stockName=" + stockName + "&count=" + count + "&price=" + price));
    }

    @Test
    public void depositTest() {
        final String username = "koalaa13";

        // create a new user
        assertEquals("Successfully added user " + username, register(username));

        // give him 100 money
        assertEquals("Added 100 money for 1 users.", deposit(username, 100));

        // try to take 200 money from him but can't do this cuz he has only 100
        assertThrows(Exception.class, () -> deposit(username, -200));

        assertEquals(100, getBalance(username));
        assertEquals("User(name=koalaa13, balance=100, stocks={})", userProfile(username));
    }

    @Test
    public void reRegisterTest() {
        final String username = "Dmozze";

        assertEquals("Successfully added user " + username, register(username));

        assertEquals("Such user already exists", register(username));
    }

    @Test
    public void buyStockTest() {
        final String username = "Darui99";

        assertEquals("Successfully added user " + username, register(username));

        assertEquals("Added 100 money for 1 users.", deposit(username, 100));

        assertEquals(1, buyStock(username, "Yandex", 1, 100));
        assertEquals(0, getBalance(username));
        assertEquals(100, userStocksValue(username));
        assertEquals("Stock: Yandex count: 1 price: 100", userStocks(username));
        assertEquals(99, getCountByStockName("Yandex"));

        // try to buy with no money
        assertEquals(0, buyStock(username, "Yandex", 1, 100));
        assertEquals(0, getBalance(username));
        assertEquals(100, userStocksValue(username));
        assertEquals("Stock: Yandex count: 1 price: 100", userStocks(username));
        assertEquals(99, getCountByStockName("Yandex"));

        assertEquals("Added 100 money for 1 users.", deposit(username, 100));
        assertEquals(100, getBalance(username));
        // trying to buy stock with price lower than on exchange
        assertEquals(0, buyStock(username, "Yandex", 1, 99));
        assertEquals(100, getBalance(username));
        assertEquals(100, userStocksValue(username));
        assertEquals("Stock: Yandex count: 1 price: 100", userStocks(username));
        assertEquals(99, getCountByStockName("Yandex"));
    }

    @Test
    public void noSuchUserTest() {
        final String nonExistingUsername = "Alex_2000";

        assertEquals("No such user", userStocks(nonExistingUsername));
        assertEquals("No such user with name " + nonExistingUsername, userProfile(nonExistingUsername));
        assertEquals(0, userStocksValue(nonExistingUsername));
        assertEquals(0, buyStock(nonExistingUsername, "Yandex", 100, 100));
        assertEquals(0, sellStock(nonExistingUsername, "Yandex", 100, 100));
    }

    @Test
    public void buyUnexistingStock() {
        final String username = "gaporf";

        assertEquals("Successfully added user " + username, register(username));

        assertEquals("Added 100 money for 1 users.", deposit(username, 100));

        final String nonExistingStock = "VK";

        assertEquals(0, buyStock(username, nonExistingStock, 1, 100));
        assertEquals(100, getBalance(username));
        assertEquals(0, userStocksValue(username));
        assertEquals("", userStocks(username));
        assertEquals(0, getCountByStockName(nonExistingStock));
    }

    @Test
    public void sellStockTest() {
        final String username = "Villen3tenmerth";
        final String stockName = "Google";

        assertEquals("Successfully added user " + username, register(username));
        assertEquals("Added 2000 money for 1 users.", deposit(username, 2000));

        assertEquals(1, buyStock(username, stockName, 1, 1100));
        assertEquals(900, getBalance(username));
        assertEquals(1000, userStocksValue(username));
        assertEquals("Stock: Google count: 1 price: 1000", userStocks(username));
        assertEquals(999, getCountByStockName(stockName));

        assertEquals("Changed price of 1 stocks", changePrice(stockName, 1500));
        assertEquals(1500, getPriceByStockName(stockName));
        assertEquals(900, getBalance(username));
        assertEquals(1500, userStocksValue(username));
        assertEquals("Stock: Google count: 1 price: 1500", userStocks(username));
        assertEquals(999, getCountByStockName(stockName));

        assertEquals(0, buyStock(username, stockName, 1, 1100));
        assertEquals(1500, getPriceByStockName(stockName));
        assertEquals(900, getBalance(username));
        assertEquals(1500, userStocksValue(username));
        assertEquals("Stock: Google count: 1 price: 1500", userStocks(username));
        assertEquals(999, getCountByStockName(stockName));

        // try to sell 2 when have only 1
        assertEquals(0, sellStock(username, stockName, 2, 1500));
        assertEquals(1500, getPriceByStockName(stockName));
        assertEquals(900, getBalance(username));
        assertEquals(1500, userStocksValue(username));
        assertEquals("Stock: Google count: 1 price: 1500", userStocks(username));
        assertEquals(999, getCountByStockName(stockName));

        assertEquals(1, sellStock(username, stockName, 1, 1400));
        assertEquals(1500, getPriceByStockName(stockName));
        assertEquals(2300, getBalance(username));
        assertEquals(0, userStocksValue(username));
        assertEquals("", userStocks(username));
        assertEquals(1000, getCountByStockName(stockName));
    }

    @Test
    public void sellStockDoNotHave() {
        final String username = "IvanMaslov";
        final String stockName = "Meta";

        assertEquals("Successfully added user " + username, register(username));
        assertEquals(0, sellStock(username, stockName, 1, 300));
        assertEquals(300, getPriceByStockName(stockName));
        assertEquals(0, getBalance(username));
        assertEquals(0, userStocksValue(username));
        assertEquals("", userStocks(username));
        assertEquals(150, getCountByStockName(stockName));
    }
}

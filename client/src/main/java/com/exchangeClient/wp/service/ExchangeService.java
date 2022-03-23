package com.exchangeClient.wp.service;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URL;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class ExchangeService {
    private final static String baseUrl = "localhost:8080";

    private String readFromUrl(final String url) {
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

    public int getStockPrice(final String name) {
        String url = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(baseUrl)
                .path("/get_price_by_name")
                .queryParam("name", name)
                .build().toUriString();
        return Integer.parseInt(readFromUrl(url));
    }

    public int buyStock(final String name, final int count, final int cost) {
        String url = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(baseUrl)
                .path("/buy")
                .queryParam("name", name)
                .queryParam("count", count)
                .queryParam("cost", cost)
                .build().toUriString();
        return Integer.parseInt(readFromUrl(url));
    }

    public int sellStock(final String name, final int count, final int cost) {
        String url = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(baseUrl)
                .path("/sell")
                .queryParam("name", name)
                .queryParam("count", count)
                .queryParam("cost", cost)
                .build().toUriString();
        return Integer.parseInt(readFromUrl(url));
    }
}

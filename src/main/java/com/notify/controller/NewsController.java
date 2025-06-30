package com.notify.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/news")
public class NewsController {
    @Value("${news.api.key}")
    private  String apiKey;

    @GetMapping
    public ResponseEntity<?> fetchNews(){
        String url = "https://gnews.io/api/v4/top-headlines?lang=en&country=in&max=5&apikey=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching news");
        }

    }

}

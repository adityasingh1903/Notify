package com.notify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notify.dto.Weather;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Service
public class WeatherService {

    private static final String API_KEY = "9b2cb7f69ad62fd5cfe8a93c8bf7c838";

    public Weather getWeatherByCoordinates(double lat, double lon) {
        try {
            String endpoint = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric",
                    lat, lon, API_KEY
            );

            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            Scanner scanner = new Scanner(is);
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) response.append(scanner.nextLine());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());

            String temp = root.path("main").path("temp").asText();
            String humidity = root.path("main").path("humidity").asText();
            String windSpeed = root.path("wind").path("speed").asText();
            String description = root.path("weather").get(0).path("description").asText();
            String city = root.path("name").asText();
            String country = root.path("sys").path("country").asText();

            return new Weather(temp, capitalize(description), city + ", " + country, humidity, windSpeed, lat, lon);

        } catch (Exception e) {
            e.printStackTrace();
            return new Weather("--", "Unable to fetch", "Unknown", "--", "--", lat, lon);
        }
    }

    // Optional fallback method if needed
    public Weather getCurrentWeather() {
        return new Weather("--", "Location not provided", "Unknown", "--", "--", null, null);
    }

    private String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}

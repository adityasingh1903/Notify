package com.notify.dto;

public class Weather {

    private String temperature;
    private String description;
    private String location;
    private String humidity;
    private String windSpeed;

    // Optional: precise coordinates
    private Double latitude;
    private Double longitude;

    // No-args constructor
    public Weather() {}

    // All-args constructor (without lat/lon)
    public Weather(String temperature, String description, String location, String humidity, String windSpeed) {
        this.temperature = temperature;
        this.description = description;
        this.location = location;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

    // Full constructor (with lat/lon)
    public Weather(String temperature, String description, String location,
                   String humidity, String windSpeed, Double latitude, Double longitude) {
        this.temperature = temperature;
        this.description = description;
        this.location = location;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getHumidity() { return humidity; }
    public void setHumidity(String humidity) { this.humidity = humidity; }

    public String getWindSpeed() { return windSpeed; }
    public void setWindSpeed(String windSpeed) { this.windSpeed = windSpeed; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    @Override
    public String toString() {
        return "Weather{" +
                "temperature='" + temperature + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", humidity='" + humidity + '\'' +
                ", windSpeed='" + windSpeed + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}

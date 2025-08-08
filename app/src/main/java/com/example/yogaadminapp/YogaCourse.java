package com.example.yogaadminapp;

import java.io.Serializable;

public class YogaCourse implements Serializable {
    private int id;
    private String dayOfWeek;       // e.g. "Monday"
    private String time;            // e.g. "10:00"
    private int capacity;           // number of people
    private int duration;           // in minutes, e.g. 60
    private double pricePerClass;   // e.g. 10.0 for £10
    private String typeOfClass;     // e.g. "Flow Yoga", "Aerial Yoga"
    private String description;     // optional



    // Constructor with all required + optional fields
    public YogaCourse( String dayOfWeek, String time, int capacity, int duration,
                      double pricePerClass, String typeOfClass, String description) {
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.capacity = capacity;
        this.duration = duration;
        this.pricePerClass = pricePerClass;
        this.typeOfClass = typeOfClass;
        this.description = description;
    }

    // Getters
    public int getId() { return id; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getTime() { return time; }
    public int getCapacity() { return capacity; }
    public int getDuration() { return duration; }
    public double getPricePerClass() { return pricePerClass; }
    public String getTypeOfClass() { return typeOfClass; }
    public String getDescription() { return description; }

    // Setters (optional but useful for frameworks or form editing)
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setTime(String time) { this.time = time; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setPricePerClass(double pricePerClass) { this.pricePerClass = pricePerClass; }
    public void setTypeOfClass(String typeOfClass) { this.typeOfClass = typeOfClass; }
    public void setDescription(String description) { this.description = description; }
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "YogaCourse{" +
                "dayOfWeek='" + dayOfWeek + '\'' +
                ", time='" + time + '\'' +
                ", capacity=" + capacity +
                ", duration=" + duration +
                ", pricePerClass=" + pricePerClass +
                ", typeOfClass='" + typeOfClass + '\'' +
                ", description='" + description + '\'' +
                '}';
    }


}
package com.example.yogaadminapp;

import java.io.Serializable;

public class YogaCourse implements Serializable {
    private String firebaseKey;
    private int id;
    private String dayOfWeek;
    private String time;
    private int capacity;
    private int duration;
    private double pricePerClass;
    private String typeOfClass;
    private String description;

    // No-argument constructor required by Firebase
    public YogaCourse() {
    }


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

    public String getFirebaseKey() {
        return firebaseKey;
    }


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
    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
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
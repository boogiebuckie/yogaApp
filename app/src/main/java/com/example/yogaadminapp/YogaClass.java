package com.example.yogaadminapp;

import java.io.Serializable;

public class YogaClass implements Serializable {
    private int id;             // Primary Key
    private int courseId;       // Foreign Key from YogaCourse
    private String dateTime;    // In ISO format e.g., "2025-08-07 14:30"
    private String teacher;
    private String comment;
    private String dayOfWeek;   // For search results display

    public YogaClass() {}

    public YogaClass(int id, int courseId, String dateTime, String teacher, String comment) {
        this.id = id;
        this.courseId = courseId;
        this.dateTime = dateTime;
        this.teacher = teacher;
        this.comment = comment;
    }

    // Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
}

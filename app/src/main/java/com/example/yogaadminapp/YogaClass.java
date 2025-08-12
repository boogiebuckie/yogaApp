package com.example.yogaadminapp;

import java.io.Serializable;

public class YogaClass implements Serializable {
    private int id;
    private int courseId;
    private String dateTime;
    private String teacher;
    private String comment;             //nam phan
    private String dayOfWeek;
    private String firebaseKey;
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

    // getters and setters for firebaseKey
    public String getFirebaseKey() {
        return firebaseKey;
    }
    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
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

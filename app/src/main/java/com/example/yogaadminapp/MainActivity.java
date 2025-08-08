package com.example.yogaadminapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        Button btnViewCourses = findViewById(R.id.btn_view_courses);
        Button btnAddCourse = findViewById(R.id.btn_add_course);
        Button btnSearchClasses = findViewById(R.id.btn_search_classes);


        btnViewCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, YogaCourseList.class);
                startActivity(intent);
            }
        });

        btnAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, YogaCourseFunctions.class);
                startActivity(intent);
            }
        });

        btnSearchClasses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ClassSearchActivity.class);
                startActivity(intent);
            }
        });


    }
}

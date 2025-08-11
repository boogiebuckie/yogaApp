package com.example.yogaadminapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class YogaCourseFunctions extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int editingCourseId = -1;

    private Spinner spinnerDay, spinnerType;
    private TimePicker timePicker;
    private EditText editCapacity, editDuration, editPrice, editDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_yoga_course_function);

        dbHelper = new DatabaseHelper(this);

        spinnerDay = findViewById(R.id.spinner_day);
        timePicker = findViewById(R.id.time_picker);
        editCapacity = findViewById(R.id.edit_capacity);
        editDuration = findViewById(R.id.edit_duration);
        editPrice = findViewById(R.id.edit_price);
        spinnerType = findViewById(R.id.spinner_type);
        editDescription = findViewById(R.id.edit_description);

        timePicker.setIs24HourView(true);

        setupSpinners();

        Intent intent = getIntent();
        if (intent.hasExtra("day")) {
            editingCourseId = intent.getIntExtra("course_id", -1);
            spinnerDay.setSelection(getSpinnerIndex(spinnerDay, intent.getStringExtra("day")));
            spinnerType.setSelection(getSpinnerIndex(spinnerType, intent.getStringExtra("type")));
            editCapacity.setText(String.valueOf(intent.getIntExtra("capacity", 0)));
            editDuration.setText(String.valueOf(intent.getIntExtra("duration", 0)));
            editPrice.setText(String.valueOf(intent.getDoubleExtra("price", 0.0)));
            editDescription.setText(intent.getStringExtra("description"));

            String time = intent.getStringExtra("time");
            if (time != null && time.contains(":")) {
                String[] parts = time.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                timePicker.setHour(hour);
                timePicker.setMinute(minute);
            }
        }
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(
                this, R.array.days_array, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.class_type_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }
    private int getSpinnerIndex(Spinner spinner, String value) {
        if (value == null) return 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }
    public void onClickAddCourse(View view) {
        try {
            String dayOfWeek = spinnerDay.getSelectedItem().toString();
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

            int capacity = Integer.parseInt(editCapacity.getText().toString().trim());
            int duration = Integer.parseInt(editDuration.getText().toString().trim());
            double price = Double.parseDouble(editPrice.getText().toString().trim());
            String typeOfClass = spinnerType.getSelectedItem().toString();
            String description = editDescription.getText().toString().trim();

            if (dayOfWeek.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please select day and time", Toast.LENGTH_SHORT).show();
                return;
            }
            if (capacity <= 0 || duration <= 0 || price < 0) {
                Toast.makeText(this, "Please enter valid capacity, duration, and price", Toast.LENGTH_SHORT).show();
                return;
            }

            YogaCourse course = new YogaCourse(dayOfWeek, time, capacity, duration, price, typeOfClass, description);

            FireBaseHelper firebaseHelper = new FireBaseHelper(this); // create instance

            if (editingCourseId == -1) {
                // New course
                long newId = dbHelper.insertCourse(course);
                course.setId((int) newId);

                // Generate new Firebase key and save it locally
                String newFirebaseKey = firebaseHelper.getCoursesRef().push().getKey(); // Add this method or make coursesRef accessible
                course.setFirebaseKey(newFirebaseKey);
                dbHelper.updateFirebaseKey(course.getId(), newFirebaseKey);

                Toast.makeText(this, "Course added locally", Toast.LENGTH_SHORT).show();

                if (firebaseHelper.isNetworkAvailable()) {
                    firebaseHelper.uploadAllCourses();
                } else {
                    Toast.makeText(this, "No internet connection. Course saved locally.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Update existing course
                course.setId(editingCourseId);

                // Retrieve existing firebaseKey from local DB for this course (implement getFirebaseKeyById)
                String existingFirebaseKey = dbHelper.getFirebaseKeyById(editingCourseId);
                if (existingFirebaseKey != null) {
                    course.setFirebaseKey(existingFirebaseKey);
                } else {
                    // Just in case, generate one
                    String newFirebaseKey = firebaseHelper.getCoursesRef().push().getKey();
                    course.setFirebaseKey(newFirebaseKey);
                    dbHelper.updateFirebaseKey(editingCourseId, newFirebaseKey);
                }

                dbHelper.updateCourse(editingCourseId, course);

                Toast.makeText(this, "Course updated locally", Toast.LENGTH_SHORT).show();

                if (firebaseHelper.isNetworkAvailable()) {
                    firebaseHelper.uploadAllCourses();
                } else {
                    Toast.makeText(this, "No internet connection. Changes saved locally.", Toast.LENGTH_SHORT).show();
                }
            }

            // Navigate back to the list
            Intent intent = new Intent(this, YogaCourseList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickDelete(View view){

    }
    public void onClickBack(View view) {
        onBackPressed();
    }
}

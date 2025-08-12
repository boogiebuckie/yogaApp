package com.example.yogaadminapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class YogaClassFunction extends AppCompatActivity {

    private DatabaseHelper classDbHelper;
    private FireBaseHelper firebaseHelper;
    private int editingClassId = -1;

    private Spinner spinnerCourse;
    private DatePicker datePicker;
    private EditText editTeacher, editComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_yoga_class_function);

        classDbHelper = new DatabaseHelper(this);
        firebaseHelper = new FireBaseHelper(this);

        spinnerCourse = findViewById(R.id.spinner_course);
        datePicker = findViewById(R.id.date_picker);
        editTeacher = findViewById(R.id.edit_teacher);
        editComment = findViewById(R.id.edit_comment);

        setupCourseSpinner();

        Intent intent = getIntent();
        int courseId = intent.getIntExtra("course_id", -1);

        if (intent.hasExtra("class_id")) {
            // Editing existing class
            editingClassId = intent.getIntExtra("class_id", -1);
            editTeacher.setText(intent.getStringExtra("teacher"));
            editComment.setText(intent.getStringExtra("comment"));

            String[] dateParts = intent.getStringExtra("dateTime").split(" ")[0].split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // zero-based month
            int day = Integer.parseInt(dateParts[2]);
            datePicker.updateDate(year, month, day);

            setSpinnerSelectionByCourseId(courseId);

        } else {
            // Creating new class: set spinner and date picker
            setSpinnerSelectionByCourseId(courseId);

            // Get the course's day of week string from DB
            String courseDayOfWeek = classDbHelper.getDayOfWeekByCourseId(courseId);
            if (courseDayOfWeek != null && !courseDayOfWeek.isEmpty()) {
                Calendar nextDate = getNextDateForDayOfWeek(courseDayOfWeek);
                datePicker.updateDate(
                        nextDate.get(Calendar.YEAR),
                        nextDate.get(Calendar.MONTH),
                        nextDate.get(Calendar.DAY_OF_MONTH)
                );
            }
        }

        Button addClassBtn = findViewById(R.id.addClass_btn);
        addClassBtn.setOnClickListener(this::onClickAddClass);
    }

    // Converts day name to Calendar.DAY_OF_WEEK constant
    private int getCalendarDayOfWeek(String dayName) {
        switch (dayName.toLowerCase(Locale.ENGLISH)) {
            case "sunday": return Calendar.SUNDAY;
            case "monday": return Calendar.MONDAY;
            case "tuesday": return Calendar.TUESDAY;
            case "wednesday": return Calendar.WEDNESDAY;
            case "thursday": return Calendar.THURSDAY;
            case "friday": return Calendar.FRIDAY;
            case "saturday": return Calendar.SATURDAY;
            default: return -1;
        }
    }

    // Gets next date from today matching the given day of week string
    private Calendar getNextDateForDayOfWeek(String dayOfWeekStr) {
        int targetDay = getCalendarDayOfWeek(dayOfWeekStr);
        if (targetDay == -1) {
            // fallback to today if unknown day
            return Calendar.getInstance();
        }

        Calendar today = Calendar.getInstance();
        int todayDay = today.get(Calendar.DAY_OF_WEEK);

        int daysUntil = targetDay - todayDay;
        if (daysUntil < 0) {
            daysUntil += 7;
        }

        today.add(Calendar.DAY_OF_MONTH, daysUntil);
        return today;
    }

    private void setupCourseSpinner() {
        List<Integer> courseIds = classDbHelper.getAllCourseIds();

        // Convert Integer list to String list
        List<String> courseIdStrings = new ArrayList<>();
        for (Integer id : courseIds) {
            courseIdStrings.add(String.valueOf(id));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courseIdStrings
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(adapter);
    }

    private void setSpinnerSelectionByCourseId(int courseId) {
        for (int i = 0; i < spinnerCourse.getCount(); i++) {
            if (Integer.parseInt((String) spinnerCourse.getItemAtPosition(i)) == courseId) {
                spinnerCourse.setSelection(i);
                break;
            }
        }
    }

    public void onClickAddClass(View view) {
        try {
            int courseId = Integer.parseInt((String) spinnerCourse.getSelectedItem());
            int year = datePicker.getYear();
            int month = datePicker.getMonth();
            int day = datePicker.getDayOfMonth();

            String teacher = editTeacher.getText().toString().trim();
            String comment = editComment.getText().toString().trim();

            if (teacher.isEmpty()) {
                Toast.makeText(this, "Please enter teacher name", Toast.LENGTH_SHORT).show();
                return;
            }

            String dateTime = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);

            YogaClass yogaClass = new YogaClass(editingClassId, courseId, dateTime, teacher, comment);

            long resultId;
            if (editingClassId == -1) {
                resultId = classDbHelper.insertClass(yogaClass);
                yogaClass.setId((int) resultId);
                Toast.makeText(this, "Class added", Toast.LENGTH_SHORT).show();
            } else {
                classDbHelper.updateClass(editingClassId, yogaClass);
                resultId = editingClassId;
                Toast.makeText(this, "Class updated", Toast.LENGTH_SHORT).show();
            }

            // Sync to Firebase
            String courseFirebaseKey = classDbHelper.getFirebaseKeyById(courseId);
            if (courseFirebaseKey != null && !courseFirebaseKey.isEmpty()) {
                firebaseHelper.uploadClassForCourse(courseFirebaseKey, yogaClass);
            } else {
                Toast.makeText(this, "Course Firebase key missing, unable to sync class", Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(this, YogaClassList.class);
            intent.putExtra("course_id", courseId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving class", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickBack(View view) {
        onBackPressed();
    }
}

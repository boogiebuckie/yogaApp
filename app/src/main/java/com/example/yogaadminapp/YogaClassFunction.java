
package com.example.yogaadminapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
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
        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_yoga_class_function);

        classDbHelper = new DatabaseHelper(this);
        firebaseHelper = new FireBaseHelper(this);
        spinnerCourse = findViewById(R.id.spinner_course);
        datePicker = findViewById(R.id.date_picker);
        editTeacher = findViewById(R.id.edit_teacher);
        editComment = findViewById(R.id.edit_comment);

        // Populate spinner with course IDs or names
        setupCourseSpinner();

        // Handle edit intent
        Intent intent = getIntent();
        int courseId = intent.getIntExtra("course_id", -1); // Get course_id for new classes too

        if (intent.hasExtra("class_id")) {
            editingClassId = intent.getIntExtra("class_id", -1);
            editTeacher.setText(intent.getStringExtra("teacher"));
            editComment.setText(intent.getStringExtra("comment"));

            String[] dateParts = intent.getStringExtra("dateTime").split(" ")[0].split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1;
            int day = Integer.parseInt(dateParts[2]);
            datePicker.updateDate(year, month, day);

            setSpinnerSelectionByCourseId(courseId);
        } else {
            // For new classes, set the spinner to the passed course_id
            setSpinnerSelectionByCourseId(courseId);
        }

        Button addClassBtn = findViewById(R.id.addClass_btn);
        addClassBtn.setOnClickListener(this::onClickAddClass);
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

            String dateTime = String.format(Locale.getDefault(), "%04d-%02d-%02d 00:00", year, month + 1, day);

            YogaClass yogaClass = new YogaClass(editingClassId, courseId, dateTime, teacher, comment);

            long resultId;
            if (editingClassId == -1) {
                // Insert locally
                resultId = classDbHelper.insertClass(yogaClass);
                yogaClass.setId((int) resultId);
                Toast.makeText(this, "Class added", Toast.LENGTH_SHORT).show();
            } else {
                // Update locally
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
            intent.putExtra("course_id", courseId); // Pass course_id back
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving class", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickBack(View view) {
        // Go back to the previous activity in the stack
        onBackPressed();
    }
}

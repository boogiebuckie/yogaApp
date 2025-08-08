package com.example.yogaadminapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ClassSearchActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ArrayList<YogaClass> searchResults;
    private SearchResultAdapter searchAdapter;
    
    private EditText editTeacherSearch, editDateSearch;
    private Spinner spinnerDaySearch;
    private ListView searchResultsList;
    private TextView tvResultsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_class_search);

        dbHelper = new DatabaseHelper(this);
        searchResults = new ArrayList<>();

        // Initialize views
        editTeacherSearch = findViewById(R.id.edit_teacher_search);
        editDateSearch = findViewById(R.id.edit_date_search);
        spinnerDaySearch = findViewById(R.id.spinner_day_search);
        searchResultsList = findViewById(R.id.search_results_list);
        tvResultsCount = findViewById(R.id.tv_results_count);

        // Setup day spinner
        setupDaySpinner();

        // Setup adapter
        searchAdapter = new SearchResultAdapter(this, searchResults);
        searchResultsList.setAdapter(searchAdapter);

        // Setup item click listener
        searchResultsList.setOnItemClickListener((parent, view, position, id) -> {
            YogaClass selectedClass = searchResults.get(position);
            showClassDetails(selectedClass);
        });
    }

    private void setupDaySpinner() {
        // Create a custom adapter with "Select Day" option
        ArrayList<String> dayOptions = new ArrayList<>();
        dayOptions.add("Select Day");
        dayOptions.add("Monday");
        dayOptions.add("Tuesday");
        dayOptions.add("Wednesday");
        dayOptions.add("Thursday");
        dayOptions.add("Friday");
        dayOptions.add("Saturday");
        dayOptions.add("Sunday");
        
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, dayOptions);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDaySearch.setAdapter(dayAdapter);
    }

    public void onClickSearch(View view) {
        String teacherName = editTeacherSearch.getText().toString().trim();
        String date = editDateSearch.getText().toString().trim();
        String dayOfWeek = spinnerDaySearch.getSelectedItem().toString();

        // Check if "Select Day" is selected
        if (dayOfWeek.equals("Select Day")) {
            dayOfWeek = "";
        }

        // Perform search
        searchResults.clear();
        
        if (!teacherName.isEmpty() || !date.isEmpty() || !dayOfWeek.isEmpty()) {
            searchResults.addAll(dbHelper.searchClassesAdvanced(teacherName, date, dayOfWeek));
        } else {
            Toast.makeText(this, "Please enter at least one search criteria", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update UI
        searchAdapter.notifyDataSetChanged();
        tvResultsCount.setText("Results: " + searchResults.size() + " classes found");

        if (searchResults.isEmpty()) {
            Toast.makeText(this, "No classes found matching your criteria", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickClear(View view) {
        editTeacherSearch.setText("");
        editDateSearch.setText("");
        spinnerDaySearch.setSelection(0);
        searchResults.clear();
        searchAdapter.notifyDataSetChanged();
        tvResultsCount.setText("Results: 0 classes found");
    }

    public void onClickBack(View view) {
        onBackPressed();
    }

    private void showClassDetails(YogaClass yogaClass) {
        // Create a dialog to show full details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Class Details");

        String details = "Date/Time: " + yogaClass.getDateTime() + "\n" +
                        "Day: " + yogaClass.getDayOfWeek() + "\n" +
                        "Teacher: " + yogaClass.getTeacher() + "\n" +
                        "Course ID: " + yogaClass.getCourseId() + "\n" +
                        "Comment: " + yogaClass.getComment();

        builder.setMessage(details);
        builder.setPositiveButton("Edit", (dialog, which) -> {
            // Navigate to edit class
            Intent intent = new Intent(this, YogaClassFunction.class);
            intent.putExtra("class_id", yogaClass.getId());
            intent.putExtra("course_id", yogaClass.getCourseId());
            intent.putExtra("dateTime", yogaClass.getDateTime());
            intent.putExtra("teacher", yogaClass.getTeacher());
            intent.putExtra("comment", yogaClass.getComment());
            startActivity(intent);
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    // Custom adapter for search results
    private class SearchResultAdapter extends ArrayAdapter<YogaClass> {

        public SearchResultAdapter(ClassSearchActivity context, ArrayList<YogaClass> classes) {
            super(context, 0, classes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            YogaClass yogaClass = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.search_result_item, parent, false);
            }

            TextView tvDateTime = convertView.findViewById(R.id.tv_search_date_time);
            TextView tvDay = convertView.findViewById(R.id.tv_search_day);
            TextView tvTeacher = convertView.findViewById(R.id.tv_search_teacher);
            TextView tvComment = convertView.findViewById(R.id.tv_search_comment);

            tvDateTime.setText(yogaClass.getDateTime());
            tvDay.setText(yogaClass.getDayOfWeek());
            tvTeacher.setText("Teacher: " + yogaClass.getTeacher());
            tvComment.setText("Comment: " + yogaClass.getComment());

            return convertView;
        }
    }
}

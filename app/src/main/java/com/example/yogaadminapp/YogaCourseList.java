
package com.example.yogaadminapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class YogaCourseList extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ArrayList<YogaCourse> courseList;
    private YogaCourseAdapter courseAdapter;
    private ListView courseListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_yoga_course_list); // use your layout name

        dbHelper = new DatabaseHelper(this);
        courseListView = findViewById(R.id.courseListView); // make sure your layout has ListView with this ID

        // Load courses from database
        courseList = dbHelper.getAllCourses();

        // Add dummy data if list is empty
        if (courseList.isEmpty()) {
            YogaCourse dummyCourse = new YogaCourse("Monday", "10:00", 20, 60, 10.0, "Flow Yoga", "Sample class");
            long courseId = dbHelper.insertCourse(dummyCourse);
            dummyCourse.setId((int) courseId);
            courseList.add(dummyCourse);
        }

        courseAdapter = new YogaCourseAdapter(this, courseList);
        courseListView.setAdapter(courseAdapter);
    }

    public void onClickAdd(View v) {
        Intent i = new Intent(this, YogaCourseFunctions.class);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        courseList.clear();
        courseList.addAll(dbHelper.getAllCourses());
        courseAdapter.notifyDataSetChanged();
    }

    // Inner class for custom adapter
    public class YogaCourseAdapter extends ArrayAdapter<YogaCourse> {

        private final DatabaseHelper dbHelper;
        private final FireBaseHelper firebaseHelper;
        private final ArrayList<YogaCourse> courses;

        public YogaCourseAdapter(Context context, ArrayList<YogaCourse> courses) {
            super(context, 0, courses);
            this.courses = courses;
            this.dbHelper = new DatabaseHelper(context);
            this.firebaseHelper = new FireBaseHelper(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            YogaCourse course = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.yoga_course_item, parent, false);
            }

            TextView tvType = convertView.findViewById(R.id.tv_type);
            TextView tvDayTime = convertView.findViewById(R.id.tv_day_time);
            TextView tvCapacity = convertView.findViewById(R.id.tv_capacity);
            TextView tvDuration = convertView.findViewById(R.id.tv_duration);
            TextView tvPrice = convertView.findViewById(R.id.tv_price);
            TextView tvDescription = convertView.findViewById(R.id.tv_description);

            tvType.setText(course.getTypeOfClass());
            tvDayTime.setText(course.getDayOfWeek() + " - " + course.getTime());
            tvCapacity.setText("Capacity: " + course.getCapacity());
            tvDuration.setText("Duration: " + course.getDuration() + " min");
            tvPrice.setText("Price: £" + course.getPricePerClass());
            tvDescription.setText(course.getDescription());

            ImageButton btnEdit = convertView.findViewById(R.id.btn_edit_course);
            ImageButton btnDelete = convertView.findViewById(R.id.btn_delete_course);
            Button btnSchedule = convertView.findViewById(R.id.btnSchedule);

            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), YogaCourseFunctions.class);
                intent.putExtra("course_id", course.getId());
                intent.putExtra("day", course.getDayOfWeek());
                intent.putExtra("time", course.getTime());
                intent.putExtra("capacity", course.getCapacity());
                intent.putExtra("duration", course.getDuration());
                intent.putExtra("price", course.getPricePerClass());
                intent.putExtra("type", course.getTypeOfClass());
                intent.putExtra("description", course.getDescription());
                getContext().startActivity(intent);
            });

            btnDelete.setOnClickListener(v -> {
                int courseId = course.getId();

                // Get firebase key first
                String firebaseKey = dbHelper.getFirebaseKeyById(courseId);

                FireBaseHelper firebaseHelper = new FireBaseHelper(getContext());

                if (firebaseKey != null && !firebaseKey.isEmpty() && firebaseHelper.isNetworkAvailable()) {
                    // Delete on Firebase
                    firebaseHelper.deleteCourseOnFirebase(firebaseKey, success -> {
                        if (success) {
                            // Delete locally after Firebase deletion
                            dbHelper.deleteCourse(courseId);
                            courseList.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(getContext(), "Deleted course remotely and locally", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to delete course on cloud", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // No firebase key or no network, delete locally only
                    dbHelper.deleteCourse(courseId);
                    courseList.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(getContext(), "Deleted course locally", Toast.LENGTH_SHORT).show();
                }
            });

            btnSchedule.setOnClickListener(v -> {
                Context context = v.getContext();
                Intent i = new Intent(context, YogaClassList.class);
                i.putExtra("course_id", course.getId());
                context.startActivity(i);
            });

            return convertView;
        }
    }


    public void OnRefreshClick(View view) {
        FireBaseHelper firebaseHelper = new FireBaseHelper(this);
        if (firebaseHelper.isNetworkAvailable()) {
            firebaseHelper.fetchCoursesFromFirebase(() -> {
                // This runs after sync finished
                runOnUiThread(() -> {
                    Toast.makeText(this, "Courses synced from cloud", Toast.LENGTH_SHORT).show();
                    // reload your course list adapter here
                    reloadCourseList();
                });
            });
        } else {
            Toast.makeText(this, "No internet connection. Course saved locally.", Toast.LENGTH_SHORT).show();
        }
    }

    private void reloadCourseList() {
        // 1. Get updated list from local database
        ArrayList<YogaCourse> updatedCourses = dbHelper.getAllCourses();

        // 2. Clear old data and add new data to your adapter's list
        courseList.clear();
        courseList.addAll(updatedCourses);

        // 3. Tell adapter to refresh the UI
        courseAdapter.notifyDataSetChanged();
    }
    public void onClickBack(View view) {
        // Go back to the previous activity in the stack
        onBackPressed();
    }
}

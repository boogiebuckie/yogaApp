
package com.example.yogaadminapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class YogaClassList extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ArrayList<YogaClass> classList;
    private YogaClassAdapter classAdapter;
    private ListView classListView;
    private int courseId; // the course we are viewing classes for

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_yoga_class_list);

        dbHelper = new DatabaseHelper(this);
        classListView = findViewById(R.id.classListView);

        // Get course ID from intent
        courseId = getIntent().getIntExtra("course_id", -1);

        // Debug: Log the course ID
        System.out.println("YogaClassList: Received course_id = " + courseId);

        // Load classes for this course
        classList = dbHelper.getClassesByCourseId(courseId);

        // Debug: Log the number of classes found
        System.out.println("YogaClassList: Found " + classList.size() + " classes for course " + courseId);

        // Add dummy data if yoga list is empty
        if (classList.isEmpty()) {
            System.out.println("YogaClassList: Adding dummy data for course " + courseId);
            // Add multiple dummy classes for testing
            classList.add(new YogaClass(0, courseId, "2025-01-15 10:00", "Sarah Johnson", "Bring your own mat"));
            classList.add(new YogaClass(0, courseId, "2025-01-17 14:30", "Mike Chen", "All levels welcome"));
            classList.add(new YogaClass(0, courseId, "2025-01-20 09:00", "Emma Davis", "Focus on breathing techniques"));
            classList.add(new YogaClass(0, courseId, "2025-01-22 16:00", "David Wilson", "Advanced poses included"));
            classList.add(new YogaClass(0, courseId, "2025-01-25 11:30", "Lisa Brown", "Relaxing session"));
            System.out.println("YogaClassList: Added " + classList.size() + " dummy classes");
        } else {
            System.out.println("YogaClassList: Found existing classes, not adding dummy data");
        }

        classAdapter = new YogaClassAdapter(this, classList);
        classListView.setAdapter(classAdapter);
        System.out.println("YogaClassList: Adapter created with " + classList.size() + " items");
    }

    public void onClickAddClass(View v) {
        Intent i = new Intent(this, YogaClassFunction.class);
        i.putExtra("course_id", courseId); // pass course ID so new class links correctly
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        classList.clear();
        classList.addAll(dbHelper.getClassesByCourseId(courseId));

        System.out.println("YogaClassList onResume: Found " + classList.size() + " classes for course " + courseId);

        // Add dummy data again if list is empty (in case user deleted all classes)
        if (classList.isEmpty()) {
            System.out.println("YogaClassList onResume: Adding dummy data for course " + courseId);
            classList.add(new YogaClass(0, courseId, "2025-01-15 10:00", "Sarah Johnson", "Bring your own mat"));
            classList.add(new YogaClass(0, courseId, "2025-01-17 14:30", "Mike Chen", "All levels welcome"));
            classList.add(new YogaClass(0, courseId, "2025-01-20 09:00", "Emma Davis", "Focus on breathing techniques"));
            classList.add(new YogaClass(0, courseId, "2025-01-22 16:00", "David Wilson", "Advanced poses included"));
            classList.add(new YogaClass(0, courseId, "2025-01-25 11:30", "Lisa Brown", "Relaxing session"));
        }

        classAdapter.notifyDataSetChanged();
        System.out.println("YogaClassList onResume: Updated adapter with " + classList.size() + " items");
    }

    // Custom adapter
    public class YogaClassAdapter extends ArrayAdapter<YogaClass> {

        private Context context;
        private ArrayList<YogaClass> classList;

        public YogaClassAdapter(Context context, ArrayList<YogaClass> classes) {
            super(context, 0, classes);
            this.context = context;
            this.classList = classes;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            YogaClass yogaClass = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.yoga_class_item, parent, false);
            }

            // Bind your views
            TextView tvDateTime = convertView.findViewById(R.id.tv_class_date_time);
            TextView tvTeacher = convertView.findViewById(R.id.tv_class_teacher);
            TextView tvComment = convertView.findViewById(R.id.tv_class_comment);

            tvDateTime.setText("Date/Time: " + yogaClass.getDateTime());
            tvTeacher.setText("Teacher: " + yogaClass.getTeacher());
            tvComment.setText("Comment: " + yogaClass.getComment());

            ImageButton btnEdit = convertView.findViewById(R.id.btn_edit_class);
            ImageButton btnDelete = convertView.findViewById(R.id.btn_delete_class);

            // Edit
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(context, YogaClassFunction.class);
                intent.putExtra("class_id", yogaClass.getId());
                intent.putExtra("course_id", yogaClass.getCourseId());
                intent.putExtra("dateTime", yogaClass.getDateTime());
                intent.putExtra("teacher", yogaClass.getTeacher());
                intent.putExtra("comment", yogaClass.getComment());
                context.startActivity(intent);
            });

            // Delete
            btnDelete.setOnClickListener(v -> {
                dbHelper.deleteClass(yogaClass.getId());
                classList.remove(position);
                notifyDataSetChanged();
                Toast.makeText(context, "Deleted class on " + yogaClass.getDateTime(), Toast.LENGTH_SHORT).show();
            });

            return convertView;
        }
    }

    public void onClickBack(View view) {
        // Go back to the previous activity in the stack
        onBackPressed();
    }
}

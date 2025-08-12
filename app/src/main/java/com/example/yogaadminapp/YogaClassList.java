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
    private FireBaseHelper firebaseHelper;

    private ArrayList<YogaClass> classList;
    private YogaClassAdapter classAdapter;
    private ListView classListView;
    private int courseId;

    private Button btnAddClass, btnRefresh, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_yoga_class_list);

        dbHelper = new DatabaseHelper(this);
        firebaseHelper = new FireBaseHelper(this);

        classListView = findViewById(R.id.classListView);
        btnAddClass = findViewById(R.id.buttonAddClass);
        btnRefresh = findViewById(R.id.fetch_class_btn);
        btnBack = findViewById(R.id.courseback_btn);

        courseId = getIntent().getIntExtra("course_id", -1);

        loadClassesFromLocal();

        classAdapter = new YogaClassAdapter(this, classList);
        classListView.setAdapter(classAdapter);

        // Button click listeners
        btnAddClass.setOnClickListener(v -> {
            Intent intent = new Intent(this, YogaClassFunction.class);
            intent.putExtra("course_id", courseId);
            startActivity(intent);
        });

        btnRefresh.setOnClickListener(v -> syncFromFirebase());

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadClassesFromLocal() {
        classList = dbHelper.getClassesByCourseId(courseId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClassesFromLocal();
        classAdapter.clear();
        classAdapter.addAll(classList);
        classAdapter.notifyDataSetChanged();
    }

    private void syncFromFirebase() {
        Toast.makeText(this, "Syncing from Firebase...", Toast.LENGTH_SHORT).show();
        firebaseHelper.fetchCoursesFromFirebase(() -> runOnUiThread(() -> {
            loadClassesFromLocal();
            classAdapter.clear();
            classAdapter.addAll(classList);
            classAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Sync complete", Toast.LENGTH_SHORT).show();
        }));
    }

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

            TextView tvDateTime = convertView.findViewById(R.id.tv_class_date_time);
            TextView tvTeacher = convertView.findViewById(R.id.tv_class_teacher);
            TextView tvComment = convertView.findViewById(R.id.tv_class_comment);

            tvDateTime.setText("Date/Time: " + yogaClass.getDateTime());
            tvTeacher.setText("Teacher: " + yogaClass.getTeacher());
            tvComment.setText("Comment: " + yogaClass.getComment());

            ImageButton btnEdit = convertView.findViewById(R.id.btn_edit_class);
            ImageButton btnDelete = convertView.findViewById(R.id.btn_delete_class);

            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(context, YogaClassFunction.class);
                intent.putExtra("class_id", yogaClass.getId());
                intent.putExtra("course_id", yogaClass.getCourseId());
                intent.putExtra("dateTime", yogaClass.getDateTime());
                intent.putExtra("teacher", yogaClass.getTeacher());
                intent.putExtra("comment", yogaClass.getComment());
                context.startActivity(intent);
            });

            btnDelete.setOnClickListener(v -> {
                int result = dbHelper.deleteClass(yogaClass.getId()); // Use yogaClass.getId() directly for deletion
                boolean deletedLocally = (result > 0);
                if (deletedLocally) {
                    String courseFirebaseKey = dbHelper.getFirebaseKeyById(yogaClass.getCourseId());
                    if (courseFirebaseKey != null && !courseFirebaseKey.isEmpty()) {
                        firebaseHelper.getCoursesRef()
                                .child(courseFirebaseKey)
                                .child("classes")
                                .child(String.valueOf(yogaClass.getId()))
                                .removeValue()
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(context, "Class deleted from Firebase", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Failed to delete class on Firebase", Toast.LENGTH_SHORT).show()
                                );
                    }
                    classList.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Deleted class on " + yogaClass.getDateTime(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete class locally", Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }
    }
}

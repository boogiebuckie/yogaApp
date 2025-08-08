package com.example.yogaadminapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FireBaseHelper {

    private DatabaseReference coursesRef;
    private Context context;
    private DatabaseHelper localDb;

    public FireBaseHelper(Context context) {
        this.context = context;
        this.localDb = new DatabaseHelper(context);
        this.coursesRef = FirebaseDatabase.getInstance().getReference("yoga_courses");
    }

    // Check internet connection
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    // Upload all courses + related classes to Firebase
    public void uploadAllCourses() {
        if (!isNetworkAvailable()) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<YogaCourse> courses = localDb.getAllCourses();
        if (courses.isEmpty()) {
            Toast.makeText(context, "No courses to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        for (YogaCourse course : courses) {
            String courseKey = String.valueOf(course.getId());
            DatabaseReference courseNode = coursesRef.child(courseKey);
            courseNode.setValue(course).addOnSuccessListener(aVoid -> {
                // Upload related classes for this course
                List<YogaClass> classes = localDb.getClassesByCourseId(course.getId());
                DatabaseReference classesNode = courseNode.child("classes");
                if (classes != null && !classes.isEmpty()) {
                    for (YogaClass yc : classes) {
                        classesNode.child(String.valueOf(yc.getId())).setValue(yc);
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to upload course " + course.getTypeOfClass(), Toast.LENGTH_SHORT).show();
            });
        }
        Toast.makeText(context, "Upload started", Toast.LENGTH_SHORT).show();
    }
//    public void deleteAllCoursesAndClasses() {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        // Delete all rows from YogaClass table
//        db.delete("YogaClass", null, null);
//
//        // Delete all rows from YogaCourse table
//        db.delete("YogaCourse", null, null);
//
//        db.close();
//    }
    // Fetch all courses + classes from Firebase and sync to local DB
    public void fetchCoursesFromFirebase() {
        if (!isNetworkAvailable()) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Delete all local courses and classes before syncing
                List<Integer> existingCourseIds = localDb.getAllCourseIds();
                for (int id : existingCourseIds) {
                    // Delete classes linked to this course first
                    List<YogaClass> classes = localDb.getClassesByCourseId(id);
                    if (classes != null) {
                        for (YogaClass yc : classes) {
                            localDb.deleteClass(yc.getId());
                        }
                    }
                    // Delete course
                    localDb.deleteCourse(id);
                }

                // Now insert fresh data from Firebase
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    YogaCourse course = courseSnapshot.getValue(YogaCourse.class);
                    if (course != null) {
                        long newCourseId = localDb.insertCourse(course);
                        course.setId((int) newCourseId);

                        DataSnapshot classesSnapshot = courseSnapshot.child("classes");
                        for (DataSnapshot classSnap : classesSnapshot.getChildren()) {
                            YogaClass yogaClass = classSnap.getValue(YogaClass.class);
                            if (yogaClass != null) {
                                yogaClass.setCourseId(course.getId()); // link local FK to new course ID
                                localDb.insertClass(yogaClass);
                            }
                        }
                    }
                }
                Toast.makeText(context, "Courses synced from cloud", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to fetch courses: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface FetchCallback {
        void onComplete();
    }

    public void fetchCoursesFromFirebase(FetchCallback callback) {
        if (!isNetworkAvailable()) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
            if (callback != null) callback.onComplete();
            return;
        }

        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear local DB first (implement this in your DatabaseHelper)
                localDb.deleteAllCoursesAndClasses();

                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    YogaCourse course = courseSnapshot.getValue(YogaCourse.class);
                    if (course != null) {
                        long newCourseId = localDb.insertCourse(course);
                        course.setId((int) newCourseId);

                        DataSnapshot classesSnapshot = courseSnapshot.child("classes");
                        for (DataSnapshot classSnap : classesSnapshot.getChildren()) {
                            YogaClass yogaClass = classSnap.getValue(YogaClass.class);
                            if (yogaClass != null) {
                                yogaClass.setCourseId(course.getId());
                                localDb.insertClass(yogaClass);
                            }
                        }
                    }
                }
                Toast.makeText(context, "Courses synced from cloud", Toast.LENGTH_SHORT).show();
                if (callback != null) callback.onComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to fetch courses: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                if (callback != null) callback.onComplete();
            }
        });
    }
}

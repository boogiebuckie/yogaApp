package com.example.yogaadminapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
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

    private final DatabaseReference coursesRef;
    private final Context context;
    private final DatabaseHelper localDb;

    public FireBaseHelper(Context context) {
        this.context = context;
        this.localDb = new DatabaseHelper(context);
        this.coursesRef = FirebaseDatabase.getInstance().getReference("yoga_courses");
    }
    public DatabaseReference getCoursesRef() {
        return coursesRef;
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

    //////////Upload all courses + related classes to Firebase
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
            String courseKey = course.getFirebaseKey();

            if (courseKey == null || courseKey.isEmpty()) {
                // New course: generate key and save to local DB
                courseKey = coursesRef.push().getKey();
                course.setFirebaseKey(courseKey);
                localDb.updateFirebaseKey(course.getId(), courseKey);
            }

            DatabaseReference courseNode = coursesRef.child(courseKey);
            courseNode.setValue(course).addOnSuccessListener(aVoid -> {
                // Upload related classes under the course node
                List<YogaClass> classes = localDb.getClassesByCourseId(course.getId());
                DatabaseReference classesNode = courseNode.child("classes");
                if (classes != null && !classes.isEmpty()) {
                    for (YogaClass yc : classes) {
                        // Use Firebase key or ID for classes (assuming ID is unique)
                        classesNode.child(String.valueOf(yc.getId())).setValue(yc);
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to upload course " + course.getTypeOfClass(), Toast.LENGTH_SHORT).show();
            });
        }
        Toast.makeText(context, "Upload started", Toast.LENGTH_SHORT).show();
    }

    ////////// Fetch all courses + classes from Firebase and sync to local DB0
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
                if (!snapshot.exists()) {
                    Toast.makeText(context, "No data found in cloud database", Toast.LENGTH_SHORT).show();
                    if (callback != null) callback.onComplete();
                    return;
                }

                // Clear local DB before syncing
                localDb.deleteAllCoursesAndClasses();

                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    YogaCourse course = courseSnapshot.getValue(YogaCourse.class);
                    if (course != null) {
                        course.setFirebaseKey(courseSnapshot.getKey());
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
                if (callback != null) callback.onComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to fetch courses: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                if (callback != null) callback.onComplete();
            }
        });
    }

    //////////delete from firebase
    public void deleteCourseOnFirebase(String firebaseKey, DeleteCallback callback) {
        if (!isNetworkAvailable()) {
            if (callback != null) callback.onComplete(false);
            return;
        }

        coursesRef.child(firebaseKey).removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onComplete(false);
                });
    }
    public interface DeleteCallback {
        void onComplete(boolean success);
    }
}

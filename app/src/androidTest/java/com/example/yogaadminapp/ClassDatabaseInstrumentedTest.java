package com.example.yogaadminapp;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ClassDatabaseInstrumentedTest {

    private Context context;
    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("YogaApp.db");
        dbHelper = new DatabaseHelper(context);
    }

    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        context.deleteDatabase("YogaApp.db");
    }

    @Test
    public void testCreateExampleYogaClassInstanceAndCrud() {
        // First insert a course to reference by class
        YogaCourse course = new YogaCourse(
                "Monday",
                "10:00",
                20,
                60,
                10.0,
                "Flow Yoga",
                "Beginner friendly"
        );
        long courseId = dbHelper.insertCourse(course);
        assertTrue(courseId > 0);

        // Example instance of YogaClass
        YogaClass cls = new YogaClass(
                -1,
                (int) courseId,
                "2025-08-07 14:30",
                "Teacher Name",
                "Sample comment"
        );

        long classRowId = dbHelper.insertClass(cls);
        assertTrue(classRowId > 0);

        ArrayList<YogaClass> classes = dbHelper.getClassesByCourseId((int) courseId);
        assertEquals(1, classes.size());
        YogaClass saved = classes.get(0);
        assertEquals("2025-08-07 14:30", saved.getDateTime());
        assertEquals("Teacher Name", saved.getTeacher());
        assertEquals("Sample comment", saved.getComment());
        assertEquals((int) courseId, saved.getCourseId());

        // Update
        YogaClass updated = new YogaClass(
                saved.getId(),
                saved.getCourseId(),
                "2025-08-08 09:00",
                "New Teacher",
                "Updated comment"
        );
        int rows = dbHelper.updateClass(saved.getId(), updated);
        assertEquals(1, rows);

        classes = dbHelper.getClassesByCourseId((int) courseId);
        assertEquals(1, classes.size());
        YogaClass afterUpdate = classes.get(0);
        assertEquals("2025-08-08 09:00", afterUpdate.getDateTime());
        assertEquals("New Teacher", afterUpdate.getTeacher());
        assertEquals("Updated comment", afterUpdate.getComment());

        // Delete
        int delRows = dbHelper.deleteClass(afterUpdate.getId());
        assertEquals(1, delRows);
        classes = dbHelper.getClassesByCourseId((int) courseId);
        assertTrue(classes.isEmpty());
    }
}

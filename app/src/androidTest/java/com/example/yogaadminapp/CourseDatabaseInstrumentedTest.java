package com.example.yogaadminapp;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class CourseDatabaseInstrumentedTest {

    private Context context;
    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        // Ensure a clean database for each test run
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
    public void testInsertAndFetchCourse() {
        YogaCourse course = new YogaCourse(
                "Monday",
                "10:00",
                20,
                60,
                10.0,
                "Flow Yoga",
                "Beginner friendly flow"
        );

        long rowId = dbHelper.insertCourse(course);
        assertTrue("Insert should return a positive rowId", rowId > 0);

        List<YogaCourse> courses = dbHelper.getAllCourses();
        assertEquals("Expect exactly one course after insert", 1, courses.size());

        YogaCourse saved = courses.get(0);
        assertEquals("Monday", saved.getDayOfWeek());
        assertEquals("10:00", saved.getTime());
        assertEquals(20, saved.getCapacity());
        assertEquals(60, saved.getDuration());
        assertEquals(10.0, saved.getPricePerClass(), 0.0001);
        assertEquals("Flow Yoga", saved.getTypeOfClass());
        assertEquals("Beginner friendly flow", saved.getDescription());
    }

    @Test
    public void testUpdateCourse() {
        YogaCourse course = new YogaCourse(
                "Tuesday",
                "11:00",
                15,
                45,
                12.5,
                "Hatha Yoga",
                "Calm and balanced"
        );
        long id = dbHelper.insertCourse(course);
        assertTrue(id > 0);

        YogaCourse updated = new YogaCourse(
                "Wednesday",
                "12:30",
                18,
                50,
                15.0,
                "Power Yoga",
                "More dynamic"
        );
        int rows = dbHelper.updateCourse((int) id, updated);
        assertEquals(1, rows);

        List<YogaCourse> courses = dbHelper.getAllCourses();
        assertEquals(1, courses.size());
        YogaCourse saved = courses.get(0);
        assertEquals("Wednesday", saved.getDayOfWeek());
        assertEquals("12:30", saved.getTime());
        assertEquals(18, saved.getCapacity());
        assertEquals(50, saved.getDuration());
        assertEquals(15.0, saved.getPricePerClass(), 0.0001);
        assertEquals("Power Yoga", saved.getTypeOfClass());
        assertEquals("More dynamic", saved.getDescription());
    }

    @Test
    public void testDeleteCourse() {
        YogaCourse course = new YogaCourse(
                "Thursday",
                "09:00",
                25,
                60,
                9.99,
                "Gentle Yoga",
                "Relaxing session"
        );
        long id = dbHelper.insertCourse(course);
        assertTrue(id > 0);

        int rows = dbHelper.deleteCourse((int) id);
        assertEquals(1, rows);

        List<YogaCourse> courses = dbHelper.getAllCourses();
        assertTrue(courses.isEmpty());
    }

    @Test
    public void testGetAllCourseIds() {
        long id1 = dbHelper.insertCourse(new YogaCourse(
                "Friday",
                "08:00",
                12,
                40,
                8.0,
                "Yin Yoga",
                "Deep stretches"
        ));
        long id2 = dbHelper.insertCourse(new YogaCourse(
                "Saturday",
                "14:00",
                30,
                90,
                20.0,
                "Aerial Yoga",
                "Fun with hammocks"
        ));

        assertTrue(id1 > 0 && id2 > 0);

        List<Integer> ids = dbHelper.getAllCourseIds();
        assertEquals(2, ids.size());
        assertTrue(ids.contains((int) id1));
        assertTrue(ids.contains((int) id2));
    }
}


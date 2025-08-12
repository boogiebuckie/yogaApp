package com.example.yogaadminapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "YogaApp.db";
    private static final int DATABASE_VERSION = 3; // Increase version for upgrade

    // YogaCourse table and columns
    private static final String TABLE_COURSE = "yoga_courses";
    private static final String COURSE_ID = "course_id";
    private static final String COURSE_DAY = "day_of_week";
    private static final String COURSE_TIME = "time";
    private static final String COURSE_CAPACITY = "capacity";
    private static final String COURSE_DURATION = "duration";
    private static final String COURSE_PRICE = "price";
    private static final String COURSE_TYPE = "type";
    private static final String COURSE_DESCRIPTION = "description";

    // NEW: Firebase key column
    private static final String COURSE_FIREBASE_KEY = "firebase_key";

    // YogaClass table and columns (unchanged)
    private static final String TABLE_CLASS = "yoga_classes";
    private static final String CLASS_ID = "class_id";
    private static final String CLASS_DATETIME = "dateTime";
    private static final String CLASS_TEACHER = "teacher";
    private static final String CLASS_COMMENT = "comment";
    private static final String CLASS_COURSE_ID = "course_id";
    private static final String CLASS_FIREBASE_KEY = "firebase_key";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createCourseTable = "CREATE TABLE " + TABLE_COURSE + " (" +
                COURSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COURSE_DAY + " TEXT NOT NULL, " +
                COURSE_TIME + " TEXT NOT NULL, " +
                COURSE_CAPACITY + " INTEGER NOT NULL, " +
                COURSE_DURATION + " INTEGER NOT NULL, " +
                COURSE_PRICE + " REAL NOT NULL, " +
                COURSE_TYPE + " TEXT NOT NULL, " +
                COURSE_DESCRIPTION + " TEXT, " +
                COURSE_FIREBASE_KEY + " TEXT" +  // new column here
                ");";

        String createClassTable = "CREATE TABLE " + TABLE_CLASS + " (" +
                CLASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CLASS_DATETIME + " TEXT NOT NULL, " +
                CLASS_TEACHER + " TEXT NOT NULL, " +
                CLASS_COMMENT + " TEXT, " +
                CLASS_COURSE_ID + " INTEGER NOT NULL, " +
                CLASS_FIREBASE_KEY + " TEXT, " +  // new firebase key column
                "FOREIGN KEY (" + CLASS_COURSE_ID + ") REFERENCES " + TABLE_COURSE + "(" + COURSE_ID + ")" +
                ");";

        db.execSQL(createCourseTable);
        db.execSQL(createClassTable);
    }

    // Handle database upgrades
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add firebase_key column without losing data
            db.execSQL("ALTER TABLE " + TABLE_COURSE + " ADD COLUMN " + COURSE_FIREBASE_KEY + " TEXT;");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_CLASS + " ADD COLUMN " + CLASS_FIREBASE_KEY + " TEXT;");
        }
    }

    // --- Modified insertCourse ---
    public long insertCourse(YogaCourse course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COURSE_DAY, course.getDayOfWeek());
        values.put(COURSE_TIME, course.getTime());
        values.put(COURSE_CAPACITY, course.getCapacity());
        values.put(COURSE_DURATION, course.getDuration());
        values.put(COURSE_PRICE, course.getPricePerClass());
        values.put(COURSE_TYPE, course.getTypeOfClass());
        values.put(COURSE_DESCRIPTION, course.getDescription());
        values.put(COURSE_FIREBASE_KEY, course.getFirebaseKey()); // new
        return db.insert(TABLE_COURSE, null, values);
    }

    // --- Modified updateCourse ---
    public int updateCourse(int id, YogaCourse course) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COURSE_DAY, course.getDayOfWeek());
        values.put(COURSE_TIME, course.getTime());
        values.put(COURSE_CAPACITY, course.getCapacity());
        values.put(COURSE_DURATION, course.getDuration());
        values.put(COURSE_PRICE, course.getPricePerClass());
        values.put(COURSE_TYPE, course.getTypeOfClass());
        values.put(COURSE_DESCRIPTION, course.getDescription());
        values.put(COURSE_FIREBASE_KEY, course.getFirebaseKey()); // new
        return db.update(TABLE_COURSE, values, COURSE_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // --- Add method to update Firebase key separately ---
    public String getFirebaseKeyById(int courseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COURSE, new String[]{"firebase_key"}, COURSE_ID + " = ?", new String[]{String.valueOf(courseId)}, null, null, null);
        String firebaseKey = null;
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex("firebase_key");
            if (index != -1) {
                firebaseKey = cursor.getString(index);
            }
        }
        cursor.close();
        return firebaseKey;
    }
    public int updateFirebaseKey(int courseId, String firebaseKey) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("firebase_key", firebaseKey);
        return db.update(TABLE_COURSE, values, COURSE_ID + " = ?", new String[]{String.valueOf(courseId)});
    }

    // --- Modified getAllCourses to load firebase key ---
    public ArrayList<YogaCourse> getAllCourses() {
        ArrayList<YogaCourse> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COURSE, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COURSE_ID));
                String day = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_DAY));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_TIME));
                int capacity = cursor.getInt(cursor.getColumnIndexOrThrow(COURSE_CAPACITY));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(COURSE_DURATION));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COURSE_PRICE));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_TYPE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_DESCRIPTION));
                String firebaseKey = null;
                int firebaseKeyIndex = cursor.getColumnIndex(COURSE_FIREBASE_KEY);
                if (firebaseKeyIndex != -1) {
                    firebaseKey = cursor.getString(firebaseKeyIndex);
                }

                YogaCourse course = new YogaCourse(day, time, capacity, duration, price, type, description);
                course.setId(id);
                course.setFirebaseKey(firebaseKey); // set firebase key
                courses.add(course);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return courses;
    }

    // --- Class CRUD ---

    public long insertClass(YogaClass yogaClass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CLASS_DATETIME, yogaClass.getDateTime());
        values.put(CLASS_TEACHER, yogaClass.getTeacher());
        values.put(CLASS_COMMENT, yogaClass.getComment());
        values.put(CLASS_COURSE_ID, yogaClass.getCourseId());
        values.put(CLASS_FIREBASE_KEY, yogaClass.getFirebaseKey());  // new
        return db.insert(TABLE_CLASS, null, values);
    }

    public int updateClass(int id, YogaClass yogaClass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CLASS_DATETIME, yogaClass.getDateTime());
        values.put(CLASS_TEACHER, yogaClass.getTeacher());
        values.put(CLASS_COMMENT, yogaClass.getComment());
        values.put(CLASS_COURSE_ID, yogaClass.getCourseId());
        values.put(CLASS_FIREBASE_KEY, yogaClass.getFirebaseKey());  // new
        return db.update(TABLE_CLASS, values, CLASS_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int deleteClass(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CLASS, CLASS_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public ArrayList<YogaClass> getAllClasses() {
        ArrayList<YogaClass> classes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CLASS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                YogaClass yogaClass = new YogaClass();
                yogaClass.setId(cursor.getInt(cursor.getColumnIndexOrThrow(CLASS_ID)));
                yogaClass.setDateTime(cursor.getString(cursor.getColumnIndexOrThrow(CLASS_DATETIME)));
                yogaClass.setTeacher(cursor.getString(cursor.getColumnIndexOrThrow(CLASS_TEACHER)));
                yogaClass.setComment(cursor.getString(cursor.getColumnIndexOrThrow(CLASS_COMMENT)));
                yogaClass.setCourseId(cursor.getInt(cursor.getColumnIndexOrThrow(CLASS_COURSE_ID)));
                int firebaseKeyIndex = cursor.getColumnIndex(CLASS_FIREBASE_KEY);
                if (firebaseKeyIndex != -1) {
                    yogaClass.setFirebaseKey(cursor.getString(firebaseKeyIndex));
                }
                classes.add(yogaClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return classes;
    }

    public ArrayList<YogaClass> getClassesByCourseId(int courseIdFilter) {
        ArrayList<YogaClass> classes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_CLASS,
                null,
                CLASS_COURSE_ID + " = ?",
                new String[]{String.valueOf(courseIdFilter)},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(CLASS_ID));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_DATETIME));
                String teacher = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_TEACHER));
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_COMMENT));
                int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(CLASS_COURSE_ID));

                YogaClass yogaClass = new YogaClass(id, courseId, dateTime, teacher, comment);
                classes.add(yogaClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return classes;
    }

    public List<Integer> getAllCourseIds() {
        List<Integer> ids = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COURSE_ID + " FROM " + TABLE_COURSE, null);
        if (cursor.moveToFirst()) {
            do {
                ids.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return ids;
    }

    public String getClassFirebaseKeyById(int classId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CLASS, new String[]{CLASS_FIREBASE_KEY}, CLASS_ID + " = ?", new String[]{String.valueOf(classId)}, null, null, null);
        String firebaseKey = null;
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(CLASS_FIREBASE_KEY);
            if (index != -1) {
                firebaseKey = cursor.getString(index);
            }
        }
        cursor.close();
        return firebaseKey;
    }

    public int updateClassFirebaseKey(int classId, String firebaseKey) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CLASS_FIREBASE_KEY, firebaseKey);
        return db.update(TABLE_CLASS, values, CLASS_ID + " = ?", new String[]{String.valueOf(classId)});
    }

    // Search methods for classes
    public ArrayList<YogaClass> searchClassesByTeacher(String teacherName) {
        ArrayList<YogaClass> classes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT c.*, co." + COURSE_DAY + " FROM " + TABLE_CLASS + " c " +
                      "JOIN " + TABLE_COURSE + " co ON c." + CLASS_COURSE_ID + " = co." + COURSE_ID + " " +
                      "WHERE c." + CLASS_TEACHER + " LIKE ? " +
                      "ORDER BY c." + CLASS_DATETIME;

        Cursor cursor = db.rawQuery(query, new String[]{"%" + teacherName + "%"});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(CLASS_ID));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_DATETIME));
                String teacher = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_TEACHER));
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_COMMENT));
                int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(CLASS_COURSE_ID));
                String dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_DAY));

                YogaClass yogaClass = new YogaClass(id, courseId, dateTime, teacher, comment);
                yogaClass.setDayOfWeek(dayOfWeek); // Store day for display
                classes.add(yogaClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return classes;
    }

    public ArrayList<YogaClass> searchClassesByDate(String date) {
        ArrayList<YogaClass> classes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT c.*, co." + COURSE_DAY + " FROM " + TABLE_CLASS + " c " +
                      "JOIN " + TABLE_COURSE + " co ON c." + CLASS_COURSE_ID + " = co." + COURSE_ID + " " +
                      "WHERE c." + CLASS_DATETIME + " LIKE ? " +
                      "ORDER BY c." + CLASS_DATETIME;

        Cursor cursor = db.rawQuery(query, new String[]{date + "%"});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(CLASS_ID));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_DATETIME));
                String teacher = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_TEACHER));
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_COMMENT));
                int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(CLASS_COURSE_ID));
                String dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_DAY));

                YogaClass yogaClass = new YogaClass(id, courseId, dateTime, teacher, comment);
                yogaClass.setDayOfWeek(dayOfWeek);
                classes.add(yogaClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return classes;
    }

    public ArrayList<YogaClass> searchClassesByDayOfWeek(String dayOfWeek) {
        ArrayList<YogaClass> classes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT c.*, co." + COURSE_DAY + " FROM " + TABLE_CLASS + " c " +
                      "JOIN " + TABLE_COURSE + " co ON c." + CLASS_COURSE_ID + " = co." + COURSE_ID + " " +
                      "WHERE co." + COURSE_DAY + " = ? " +
                      "ORDER BY c." + CLASS_DATETIME;

        Cursor cursor = db.rawQuery(query, new String[]{dayOfWeek});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(CLASS_ID));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_DATETIME));
                String teacher = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_TEACHER));
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_COMMENT));
                int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(CLASS_COURSE_ID));
                String day = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_DAY));

                YogaClass yogaClass = new YogaClass(id, courseId, dateTime, teacher, comment);
                yogaClass.setDayOfWeek(day);
                classes.add(yogaClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return classes;
    }

    public ArrayList<YogaClass> searchClassesAdvanced(String teacherName, String date, String dayOfWeek) {
        ArrayList<YogaClass> classes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT c.*, co.").append(COURSE_DAY).append(" FROM ").append(TABLE_CLASS).append(" c ");
        queryBuilder.append("JOIN ").append(TABLE_COURSE).append(" co ON c.").append(CLASS_COURSE_ID).append(" = co.").append(COURSE_ID).append(" ");
        queryBuilder.append("WHERE 1=1 ");

        ArrayList<String> params = new ArrayList<>();

        if (teacherName != null && !teacherName.trim().isEmpty()) {
            queryBuilder.append("AND c.").append(CLASS_TEACHER).append(" LIKE ? ");
            params.add("%" + teacherName + "%");
        }

        if (date != null && !date.trim().isEmpty()) {
            queryBuilder.append("AND c.").append(CLASS_DATETIME).append(" LIKE ? ");
            params.add(date + "%");
        }

        if (dayOfWeek != null && !dayOfWeek.trim().isEmpty()) {
            queryBuilder.append("AND co.").append(COURSE_DAY).append(" = ? ");
            params.add(dayOfWeek);
        }

        queryBuilder.append("ORDER BY c.").append(CLASS_DATETIME);

        Cursor cursor = db.rawQuery(queryBuilder.toString(), params.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(CLASS_ID));
                String dateTime = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_DATETIME));
                String teacher = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_TEACHER));
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(CLASS_COMMENT));
                int courseId = cursor.getInt(cursor.getColumnIndexOrThrow(CLASS_COURSE_ID));
                String day = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_DAY));

                YogaClass yogaClass = new YogaClass(id, courseId, dateTime, teacher, comment);
                yogaClass.setDayOfWeek(day);
                classes.add(yogaClass);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return classes;
    }
    public void deleteAllCoursesAndClasses() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete all rows from yoga_classes first, because it references yoga_courses
        db.delete(TABLE_CLASS, null, null);
        // Then delete all rows from yoga_courses
        db.delete(TABLE_COURSE, null, null);
        db.close();
    }
    public int deleteCourse(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete classes linked to this course
        db.delete(TABLE_CLASS, CLASS_COURSE_ID + " = ?", new String[]{String.valueOf(id)});
        // Delete course
        return db.delete(TABLE_COURSE, COURSE_ID + " = ?", new String[]{String.valueOf(id)});
    }
}

package com.example.focusfrenzy;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;

public class SQLiteManager extends SQLiteOpenHelper {

    private static SQLiteManager sqLiteManager;

    private static final String DATABASE_NAME = "focusfrenzy_database.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "reminders";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_POMODORO = "usePomodoro"; // 0 = false, 1 = true

    // Singleton: only one instance
    public static SQLiteManager getInstance(Context context) {
        if (sqLiteManager == null) {
            sqLiteManager = new SQLiteManager(context.getApplicationContext());
        }
        return sqLiteManager;
    }

    private SQLiteManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_POMODORO + " INTEGER" +
                ");";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        onCreate(db);
    }

    // Insert a new reminder
    public long addReminder(String title, String date, boolean usePomodoro) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_POMODORO, usePomodoro ? 1 : 0);
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    // NEW METHOD: Check if a reminder already exists
    public boolean reminderExists(String title, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COLUMN_ID},
                COLUMN_TITLE + "=? AND " + COLUMN_DATE + "=?",
                new String[]{title, date},
                null, null, null
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // Get all reminders
    public Cursor getAllReminders() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_DATE, COLUMN_POMODORO},
                null, null, null, null,
                COLUMN_ID + " DESC");
    }

    // Get a reminder by ID
    public Cursor getReminder(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME,
                new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_DATE, COLUMN_POMODORO},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
    }

    // Delete a reminder by ID
    public void deleteReminder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }
}

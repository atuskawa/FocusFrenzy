package com.example.focusfrenzy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteManager extends SQLiteOpenHelper {
    private static SQLiteManager sqLiteManager;
    private static final String DATABASE_NAME = "focusfrenzy_database.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "reminders";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_POMODORO = "usePomodoro";

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
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_POMODORO + " INTEGER);";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old, int n) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addReminder(String title, String date, boolean usePomodoro) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_POMODORO, usePomodoro ? 1 : 0);
        return db.insert(TABLE_NAME, null, values);
    }

    public void updateReminder(int id, String title, String date, boolean usePomodoro) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_POMODORO, usePomodoro ? 1 : 0);
        db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteReminder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Cursor getAllReminders() {
        return getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, COLUMN_ID + " DESC");
    }
    public Cursor searchReminders(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        // The % signs are wildcards, so it finds the text anywhere in the title
        return db.query(TABLE_NAME,
                null,
                COLUMN_TITLE + " LIKE ?",
                new String[]{"%" + query + "%"},
                null, null, COLUMN_ID + " DESC");
    }
}
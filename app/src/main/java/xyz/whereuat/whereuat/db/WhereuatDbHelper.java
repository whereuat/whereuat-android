package xyz.whereuat.whereuat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import xyz.whereuat.whereuat.db.entry.ContactEntry;
import xyz.whereuat.whereuat.db.entry.KeyLocationEntry;

/**
 * Created by kangp3 on 4/5/16.
 */

public class WhereuatDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "whereuat.db";

    public WhereuatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ContactEntry.SQL_CREATE_ENTRIES);
        db.execSQL(KeyLocationEntry.SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(ContactEntry.SQL_DELETE_ENTRIES);
        db.execSQL(KeyLocationEntry.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

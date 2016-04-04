package com.whereuat.whereu.db;

import android.provider.BaseColumns;
import android.util.Log;
import android.util.Pair;

import java.security.Key;

/**
 * Created by kangp3 on 4/3/16.
 */
public final class WhereuatContract {
    private static final String TAG = "WuaContract";

    public WhereuatContract() {}

    public static abstract class ContactEntry implements BaseColumns {
        public static final String TABLE_NAME = "contacts";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_PHONE = "phone";
        public static final String COLUMN_NAME_AUTOSHARE = "autoshare";
    }

    public static abstract class KeyLocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "key_locations";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_LATITUDE = "lat";
        public static final String COLUMN_NAME_LONGITUDE = "lng";
    }

    @SafeVarargs
    public static String createTable(String table_name, Pair<String, String>... columns) {
        String sql_string = "CREATE TABLE " + table_name;
        if (columns.length == 0) {
            Log.d(TAG, "No columns provided for table " + table_name);
            return sql_string;
        }
        sql_string += "(";
        for (Pair<String, String> column: columns) {
            sql_string += column.first + " " + column.second + ",";
        }
        sql_string = sql_string.substring(0, sql_string.length()-1);
        sql_string += ");";

        return sql_string;
    }

    public static String dropTableIfExists(String table_name) {
        return "DROP TABLE IF EXISTS " + table_name + ";";
    }

    public static final String SQL_CREATE_ENTRIES =
            createTable(ContactEntry.TABLE_NAME,
                        Pair.create(ContactEntry._ID, "INTEGER PRIMARY KEY"),
                        Pair.create(ContactEntry.COLUMN_NAME_NAME, "VARCHAR"),
                        Pair.create(ContactEntry.COLUMN_NAME_PHONE, "VARCHAR"),
                        Pair.create(ContactEntry.COLUMN_NAME_AUTOSHARE, "BOOLEAN")) +
            createTable(KeyLocationEntry.TABLE_NAME,
                        Pair.create(KeyLocationEntry._ID, "INTEGER PRIMARY KEY"),
                        Pair.create(KeyLocationEntry.COLUMN_NAME_NAME, "VARCHAR"),
                        Pair.create(KeyLocationEntry.COLUMN_NAME_LATITUDE, "REAL"),
                        Pair.create(KeyLocationEntry.COLUMN_NAME_LONGITUDE, "REAL"));

    public static final String SQL_DELETE_ENTRIES =
            dropTableIfExists(ContactEntry.TABLE_NAME) +
            dropTableIfExists(KeyLocationEntry.TABLE_NAME);
}

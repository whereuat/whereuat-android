package xyz.whereuat.whereuat.db;

import android.util.Log;
import android.util.Pair;

import xyz.whereuat.whereuat.db.entry.ContactEntry;
import xyz.whereuat.whereuat.db.entry.KeyLocationEntry;

/**
 * Created by kangp3 on 4/3/16.
 */
public final class WhereuatContract {
    private static final String TAG = "WuaContract";

    public WhereuatContract() {}

    // column_name_type is a list of pairs of column names and their types
    @SafeVarargs
    private static String createTable(String table_name, Pair<String, String>... column_name_types) {
        if (column_name_types.length == 0) {
            Log.d(TAG, "No columns provided for table " + table_name);
            return "";
        }
        String schema = "";
        for (Pair<String, String> column_name_type: column_name_types) {
            schema += String.format("%s %s,", column_name_type.first, column_name_type.second);
        }
        schema = schema.substring(0, schema.length()-1);
        return String.format("CREATE TABLE %s (%s);", table_name, schema);
    }

    private static String dropTableIfExists(String table_name) {
        return String.format("DROP TABLE IF EXISTS %s;", table_name);
    }

    public static final String SQL_CREATE_ENTRIES =
            createTable(ContactEntry.TABLE_NAME,
                        Pair.create(ContactEntry._ID, "INTEGER PRIMARY KEY"),
                        Pair.create(ContactEntry.COLUMN_NAME, "VARCHAR"),
                        Pair.create(ContactEntry.COLUMN_PHONE, "VARCHAR"),
                        Pair.create(ContactEntry.COLUMN_AUTOSHARE, "BOOLEAN")) +
            createTable(KeyLocationEntry.TABLE_NAME,
                        Pair.create(KeyLocationEntry._ID, "INTEGER PRIMARY KEY"),
                        Pair.create(KeyLocationEntry.COLUMN_NAME_NAME, "VARCHAR"),
                        Pair.create(KeyLocationEntry.COLUMN_NAME_LATITUDE, "REAL"),
                        Pair.create(KeyLocationEntry.COLUMN_NAME_LONGITUDE, "REAL"));

    public static final String SQL_DELETE_ENTRIES =
            dropTableIfExists(ContactEntry.TABLE_NAME) +
            dropTableIfExists(KeyLocationEntry.TABLE_NAME);
}

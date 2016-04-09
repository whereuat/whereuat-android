package xyz.whereuat.whereuat.db.entry;

import android.util.Log;

import java.util.AbstractMap.SimpleEntry;

/**
 * Created by kangp3 on 4/9/16.
 */
public class Entry {
    private static final String TAG = "Entry";

    public Entry() { }

    // column_name_type is a list of pairs of column names and their types
    @SafeVarargs
    protected static String createTable(String table_name,
                                        SimpleEntry<String, String>... column_name_types) {
        if (column_name_types.length == 0) {
            Log.d(TAG, "No columns provided for table " + table_name);
            return "";
        }
        String schema = "";
        for (SimpleEntry<String, String> column_name_type: column_name_types) {
            schema += String.format("%s %s,", column_name_type.getKey(),
                    column_name_type.getValue());
        }
        schema = schema.substring(0, schema.length()-1);
        return String.format("CREATE TABLE %s (%s);", table_name, schema);
    }

    protected static String dropTableIfExists(String table_name) {
        return String.format("DROP TABLE IF EXISTS %s;", table_name);
    }
}


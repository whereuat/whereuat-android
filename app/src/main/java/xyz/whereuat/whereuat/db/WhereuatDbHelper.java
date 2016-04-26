package xyz.whereuat.whereuat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.AbstractMap.SimpleEntry;

import xyz.whereuat.whereuat.db.entry.ContactEntry;
import xyz.whereuat.whereuat.db.entry.KeyLocationEntry;

/**
 * <p>The WhereuatDbHelper class handles the creation, upgrading, and deletion of the SQLite tables
 * necessary for the whereu@ application.</p>
 */

public class WhereuatDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "whereuat.db";

    private static String TAG = "DbHelper";

    /**
     * WhereuatDbHelper constructor.
     *
     * @param context The Context to build the database helper in
     */
    public WhereuatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Method called when the database is first created. Sets up tables with the entries defined in
     * the {@link xyz.whereuat.whereuat.db.entry} package.
     *
     * @param db The SQLiteDatabase to be created
     */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ContactEntry.SQL_CREATE_ENTRIES);
        db.execSQL(KeyLocationEntry.SQL_CREATE_ENTRIES);
    }

    /**
     * Method called when the database is upgraded from one version to another.
     *
     * @param db The SQLiteDatabase to be upgraded
     * @param oldVersion The version number of the database to be upgraded from
     * @param newVersion The version number of the database to be upgraded to
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(ContactEntry.SQL_DELETE_ENTRIES);
        db.execSQL(KeyLocationEntry.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * Method called when the database is downgraded from one version to another
     *
     * @param db The SQLiteDatabase to be downgraded
     * @param oldVersion The version number of the database to be downgraded to
     * @param newVersion The version number of the database to be downgraded from
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Helper function for the entries to generate the SQL strings used to create their tables in
     * {{@link #onCreate(SQLiteDatabase)}}
     *
     * @param table_name Name of the SQL table to be created
     * @param column_name_types SimpleEntries of each column and its corresponding SQL data type
     * @return The SQL string to create the table with the inputted columns
     */
    @SafeVarargs
    public static String createTableSql(String table_name,
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

    /**
     * Helper function for the entries to generate the SQL strings used to drop their tables in
     * {{@link #onUpgrade(SQLiteDatabase, int, int)}}
     *
     * @param table_name Name of the SQL table to be dropped
     * @return The SQL String to drop the table
     */
    public static String dropTableIfExistsSql(String table_name) {
        return String.format("DROP TABLE IF EXISTS %s;", table_name);
    }
}

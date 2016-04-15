package xyz.whereuat.whereuat;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;

import xyz.whereuat.whereuat.db.command.InsertCommand;
import xyz.whereuat.whereuat.db.entry.KeyLocationEntry;

/**
 * Created by kangp3 on 4/10/16.
 */
public class KeyLocationUtils {
    private static final String TAG = "KeyLocObj";

    public static boolean nameIsValid(String name) {
        return name != null && name.length() > 0;
    }

    public static boolean locIsValid(Location loc) {
        return loc != null;
    }

    public static InsertCommand buildInsertCommand(Context context, String name, double latitude,
                                                   double longitude) {
        ContentValues values = new ContentValues();
        values.put(KeyLocationEntry.COLUMN_NAME, name);
        values.put(KeyLocationEntry.COLUMN_LATITUDE, latitude);
        values.put(KeyLocationEntry.COLUMN_LONGITUDE, longitude);

        return new InsertCommand(context, KeyLocationEntry.TABLE_NAME, null, values);
    }
}

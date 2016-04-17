package xyz.whereuat.whereuat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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

    public static String getNearestLoc(Cursor c, Location origin) throws IllegalArgumentException {
        String nearest = null;
        double nearest_dist = Double.MAX_VALUE;

        c.moveToFirst();
        while(c.moveToNext()) {
            double longitude = c.getDouble(c.getColumnIndexOrThrow(
                    KeyLocationEntry.COLUMN_LONGITUDE));
            double latitude = c.getDouble(c.getColumnIndexOrThrow(
                    KeyLocationEntry.COLUMN_LATITUDE));

            Location loc = new Location("");
            loc.setLongitude(longitude);
            loc.setLatitude(latitude);

            double dist = loc.distanceTo(origin);
            if (dist < nearest_dist) {
                nearest = c.getString(c.getColumnIndexOrThrow(KeyLocationEntry.COLUMN_NAME));
                nearest_dist = dist;
            }
        }

        return nearest;
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

package xyz.whereuat.whereuat.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import xyz.whereuat.whereuat.db.command.InsertCommand;
import xyz.whereuat.whereuat.db.command.QueryCommand;
import xyz.whereuat.whereuat.db.entry.KeyLocationEntry;

/**
 * Created by kangp3 on 4/10/16.
 */
public class KeyLocationUtils {
    private static final String TAG = "KeyLocObj";

    public static class KeyLocation {
        public String name = null;
        public Location loc = null;

        public KeyLocation() { }
        public KeyLocation(String name_, double latitude_, double longitude_) {
            loc = new Location("");
            loc.setLatitude(latitude_);
            loc.setLongitude(longitude_);

            name = name_;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            try {
                if (name != null) {
                    json.put("name", name);

                    JSONObject geometry_json = new JSONObject();

                    JSONObject location_json = new JSONObject();
                    location_json.put("lat", loc.getLatitude());
                    location_json.put("lng", loc.getLongitude());

                    geometry_json.put("location", location_json);

                    json.put("geometry", geometry_json);
                } else {
                    return null;
                }
            } catch (JSONException e) {
                Log.d(TAG, "Error in key location JSON construction");
                throw e;
            }
            return json;
        }
    }

    public static boolean nameIsValid(String name) {
        return name != null && name.length() > 0;
    }

    public static boolean locIsValid(Location loc) {
        return loc != null;
    }

    // Cursor c is the result of a select all query over the key locations
    public static KeyLocation findNearestLoc(Cursor c, Location origin) throws
            IllegalArgumentException {
        KeyLocation nearest = new KeyLocation();
        double nearest_dist = Double.MAX_VALUE;

        c.moveToFirst();
        do {
            double longitude = c.getDouble(c.getColumnIndexOrThrow(
                    KeyLocationEntry.COLUMN_LONGITUDE));
            double latitude = c.getDouble(c.getColumnIndexOrThrow(
                    KeyLocationEntry.COLUMN_LATITUDE));

            Location loc = new Location("");
            loc.setLongitude(longitude);
            loc.setLatitude(latitude);

            double dist = loc.distanceTo(origin);
            if (dist < nearest_dist) {
                String name = c.getString(c.getColumnIndexOrThrow(KeyLocationEntry.COLUMN_NAME));
                double lt = c.getDouble(c.getColumnIndexOrThrow(KeyLocationEntry.COLUMN_LATITUDE));
                double lg = c.getDouble(c.getColumnIndexOrThrow(KeyLocationEntry.COLUMN_LONGITUDE));

                nearest = new KeyLocation(name, lt, lg);
                nearest_dist = dist;
            }
        } while(c.moveToNext());
        
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

    public static QueryCommand buildSelectAllCommand(Context context, String[] select_cols) {
        return new QueryCommand(context, KeyLocationEntry.TABLE_NAME, false, select_cols, null,
                null, null, null, null, null);
    }
}

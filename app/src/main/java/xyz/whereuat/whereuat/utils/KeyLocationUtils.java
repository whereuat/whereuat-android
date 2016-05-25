package xyz.whereuat.whereuat.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import xyz.whereuat.whereuat.db.command.DeleteCommand;
import xyz.whereuat.whereuat.db.command.InsertCommand;
import xyz.whereuat.whereuat.db.command.QueryCommand;
import xyz.whereuat.whereuat.db.command.UpdateCommand;
import xyz.whereuat.whereuat.db.entry.KeyLocationEntry;

/**
 * Utilities for the key_locations table of the database
 */
public class KeyLocationUtils {
    private static final String TAG = "KeyLocObj";

    /**
     * KeyLocation utility class to store data about a key location and convert it to JSON
     */
    public static class KeyLocation {
        public String name = null;
        public Location loc = null;

        /**
         * Default constructor for KeyLocation
         */
        public KeyLocation() { }

        /**
         * Constructor for KeyLocation
         *
         * @param name_ Name of the key location
         * @param latitude_ Latitude of the key location
         * @param longitude_ Longitude of the key location
         */
        public KeyLocation(String name_, double latitude_, double longitude_) {
            loc = new Location("");
            loc.setLatitude(latitude_);
            loc.setLongitude(longitude_);

            name = name_;
        }

        /**
         * Method to convert the KeyLocation object to a JSON object for packaging into POST
         * requests
         *
         * @return JSONObject with the structure expected by the server
         * @throws JSONException if building the JSON object fails
         */
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

    /**
     * Method to check if a key location's name is valid (non-null and non-empty)
     *
     * @param name Name for validity check
     * @return true if inputted name is valid
     */
    public static boolean nameIsValid(String name) {
        return name != null && name.length() > 0;
    }

    /**
     * Method to check if a location is valid (non-null)
     *
     * @param loc Location for validity check
     * @return true if inputted location is valid
     */
    public static boolean locIsValid(Location loc) {
        return loc != null;
    }

    /**
     * Method to find the nearest location to the inputted origin Location
     *
     * @param c Cursor to set of a selection over all key location
     * @param origin Point to find nearest key location to (generally the user's current location)
     * @return Nearest key location to origin
     * @throws IllegalArgumentException if the Cursor does not contain the necessary latitude,
     *                                  longitude, and name columns
     */
    public static KeyLocation findNearestLoc(Cursor c, Location origin) throws
            IllegalArgumentException {
        // Initialize the nearest key location to null and the nearest_distance to infinity
        KeyLocation nearest = new KeyLocation();
        double nearest_dist = Double.MAX_VALUE;

        c.moveToFirst();
        do {
            // Iterate over all of the key locations and find the one nearest to the origin
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

    /**
     * Method to build a command to insert a key location into the table
     *
     * @param context Context to build the command
     * @param name Name of the key location to be inserted
     * @param latitude Latitude of the key location to be inserted
     * @param longitude Longitude of the key location to b inesrted
     * @return InsertCommand to insert the given key location
     */
    public static InsertCommand buildInsertCommand(Context context, String name, double latitude,
                                                   double longitude) {
        ContentValues values = new ContentValues();
        values.put(KeyLocationEntry.COLUMN_NAME, name);
        values.put(KeyLocationEntry.COLUMN_LATITUDE, latitude);
        values.put(KeyLocationEntry.COLUMN_LONGITUDE, longitude);

        return new InsertCommand(context, KeyLocationEntry.TABLE_NAME, null, values);
    }

    /**
     * Method to build a command to select a key location by name
     *
     * @param context Context to build the command
     * @param select_cols Columns to be included in the query's result
     * @param name Name to be queried
     * @return QueryCommand to query for key locations with the given name
     */
    public static QueryCommand buildSelectNameCommand(Context context, String[] select_cols,
                                                      String name) {
        String selection = String.format("%s=?", KeyLocationEntry.COLUMN_NAME);
        return new QueryCommand(context, KeyLocationEntry.TABLE_NAME, false, select_cols, selection,
                new String[] {name}, null, null, null, null);
    }

    /**
     * Method to build a command to select all key locations
     *
     * @param context Context to build the command
     * @return QueryCommand to query over all of the key locations
     */
    public static QueryCommand buildSelectAllCommand(Context context) {
        return new QueryCommand(context, KeyLocationEntry.TABLE_NAME, false,
                KeyLocationEntry.COLUMNS, null, null, null, null, null, null);
    }

    /**
     * Method to build a command to update a key location name by the key location's id in the
     * database.
     *
     * @param context the context to build the command
     * @param id the id of the key location to be updated
     * @param new_name the new name of the key location to update
     * @return UpdateCommand to update the desired key location's name
     */
    public static UpdateCommand buildUpdateNameByIdCommand(Context context, Integer id,
                                                           String new_name) {
        String where = String.format("%s=?", KeyLocationEntry._ID);
        ContentValues values = new ContentValues();
        values.put(KeyLocationEntry.COLUMN_NAME, new_name);
        return new UpdateCommand(context, KeyLocationEntry.TABLE_NAME, values, where,
                new String[] {id.toString()});
    }

    public static DeleteCommand buildDeleteByIdCommand(Context context, Integer id) {
        String where = String.format("%s=?", KeyLocationEntry._ID);
        return new DeleteCommand(context, KeyLocationEntry.TABLE_NAME, where,
                new String[] {id.toString()});
    }
}

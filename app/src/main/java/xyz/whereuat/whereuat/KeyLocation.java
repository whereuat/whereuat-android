package xyz.whereuat.whereuat;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import xyz.whereuat.whereuat.db.DbObject;
import xyz.whereuat.whereuat.db.DbTask;
import xyz.whereuat.whereuat.db.command.InsertCommand;
import xyz.whereuat.whereuat.db.entry.ContactEntry;
import xyz.whereuat.whereuat.db.entry.KeyLocationEntry;

/**
 * Created by kangp3 on 4/10/16.
 */
public class KeyLocation implements DbObject {
    public final String name;
    public final double latitude;
    public final double longitude;

    private static final String TAG = "KeyLocObj";

    public KeyLocation(String _name, double _latitude, double _longitude) {
        name = _name;
        latitude = _latitude;
        longitude = _longitude;
    }

    public static boolean nameIsValid(String name) {
        return name != null && name.length() > 0;
    }

    public static boolean locIsValid(Location loc) {
        return loc != null;
    }

    public void dbInsert(Context context) {
        ContentValues values = new ContentValues();
        values.put(KeyLocationEntry.COLUMN_NAME, name);
        values.put(KeyLocationEntry.COLUMN_LATITUDE, latitude);
        values.put(KeyLocationEntry.COLUMN_LONGITUDE, longitude);

        InsertCommand cmd = new InsertCommand(context, KeyLocationEntry.TABLE_NAME, null, values);
        new DbTask(new DbTask.AsyncResponse() {
            @Override
            public void processFinish(Object result) {
                if ((Long) result != -1) {
                    Log.d(TAG, "Successfully inserted");
                } else {
                    Log.d(TAG, "Some weird things happened when inserting into DB");
                }
            }
        }).execute(cmd);
    }
}

package xyz.whereuat.whereuat.db.entry;

import android.provider.BaseColumns;

/**
 * Created by kangp3 on 4/5/16.
 */
public final class KeyLocationEntry implements BaseColumns {
    public static final String TABLE_NAME = "key_locations";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_LATITUDE = "lat";
    public static final String COLUMN_NAME_LONGITUDE = "lng";
}

package xyz.whereuat.whereuat.db.entry;

import android.provider.BaseColumns;

import java.security.Key;
import java.util.AbstractMap.SimpleEntry;

/**
 * Created by kangp3 on 4/5/16.
 */
public final class KeyLocationEntry extends Entry implements BaseColumns {
    public static final String TABLE_NAME = "key_locations";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LATITUDE = "lat";
    public static final String COLUMN_LONGITUDE = "lng";

    public KeyLocationEntry() { }

    public static final String SQL_CREATE_ENTRIES =
            createTable(TABLE_NAME,
                        new SimpleEntry<>(_ID, "INTEGER PRIMARY KEY"),
                        new SimpleEntry<>(COLUMN_NAME, "VARCHAR"),
                        new SimpleEntry<>(COLUMN_LATITUDE, "REAL"),
                        new SimpleEntry<>(COLUMN_LONGITUDE, "REAL"));

    public static final String SQL_DELETE_ENTRIES =
            dropTableIfExists(TABLE_NAME);
}

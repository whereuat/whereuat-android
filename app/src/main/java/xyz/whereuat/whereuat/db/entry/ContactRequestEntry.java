package xyz.whereuat.whereuat.db.entry;

import android.provider.BaseColumns;

import java.util.AbstractMap;

import xyz.whereuat.whereuat.db.WhereuatDbHelper;

/**
 * Created by julius on 5/13/16.
 */
public class ContactRequestEntry implements BaseColumns {
    public static final String TABLE_NAME = "contact_requests";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String[] COLUMNS = new String[] {COLUMN_NAME, COLUMN_PHONE, _ID};

    public static final String SQL_CREATE_ENTRIES =
            WhereuatDbHelper.createTableSql(TABLE_NAME,
                    new AbstractMap.SimpleEntry<>(_ID, "INTEGER PRIMARY KEY"),
                    new AbstractMap.SimpleEntry<>(COLUMN_NAME, "VARCHAR"),
                    new AbstractMap.SimpleEntry<>(COLUMN_PHONE, "VARCHAR"));

    public static final String SQL_DELETE_ENTRIES =
            WhereuatDbHelper.dropTableIfExistsSql(TABLE_NAME);
}

package xyz.whereuat.whereuat.db.entry;

import android.provider.BaseColumns;

import java.util.AbstractMap.SimpleEntry;

import xyz.whereuat.whereuat.db.WhereuatDbHelper;

/**
 * Created by kangp3 on 4/5/16.
 */
public final class ContactEntry implements BaseColumns {
    public static final String TABLE_NAME = "contacts";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_AUTOSHARE = "autoshare";
    public static final String COLUMN_COLOR = "color";

    public ContactEntry() { }

    public static final String SQL_CREATE_ENTRIES =
            WhereuatDbHelper.createTableSql(TABLE_NAME,
                                         new SimpleEntry<>(_ID, "INTEGER PRIMARY KEY"),
                                         new SimpleEntry<>(COLUMN_NAME, "VARCHAR"),
                                         new SimpleEntry<>(COLUMN_PHONE, "VARCHAR"),
                                         new SimpleEntry<>(COLUMN_AUTOSHARE, "BOOLEAN"),
                                         new SimpleEntry<>(COLUMN_COLOR, "INT"));

    public static final String SQL_DELETE_ENTRIES =
            WhereuatDbHelper.dropTableIfExistsSql(TABLE_NAME);
}

package xyz.whereuat.whereuat.db.entry;

import android.provider.BaseColumns;

/**
 * Created by kangp3 on 4/5/16.
 */
public final class ContactEntry implements BaseColumns {
    public static final String TABLE_NAME = "contacts";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_AUTOSHARE = "autoshare";
}

package xyz.whereuat.whereuat;

import android.content.ContentValues;
import android.content.Context;

import xyz.whereuat.whereuat.db.command.InsertCommand;
import xyz.whereuat.whereuat.db.command.QueryCommand;
import xyz.whereuat.whereuat.db.command.UpdateCommand;
import xyz.whereuat.whereuat.db.entry.ContactEntry;

/**
 * Created by kangp3 on 4/10/16.
 */
public class ContactUtils {
    private static final String TAG = "ContactObj";

    public static InsertCommand buildInsertCommand(Context context, String name, String phone,
                                                   boolean autoshare) {
        ContentValues values = new ContentValues();
        values.put(ContactEntry.COLUMN_NAME, name);
        values.put(ContactEntry.COLUMN_PHONE, phone);
        values.put(ContactEntry.COLUMN_AUTOSHARE, autoshare);

        return new InsertCommand(context, ContactEntry.TABLE_NAME, null, values);
    }

    public static QueryCommand buildSelectContactByPhoneCommand(Context context, String phone,
                                                                String[] select_cols) {
        String selection = String.format("%s=?", ContactEntry.COLUMN_PHONE);
        return new QueryCommand(context, ContactEntry.TABLE_NAME, true, select_cols, selection,
                new String[] {phone}, null, null, null, null);
    }

    public static QueryCommand buildSelectAllCommand(Context context, String[] select_cols) {
        return new QueryCommand(context, ContactEntry.TABLE_NAME, false, select_cols, null, null,
                                null, null, null, null);
    }

    public static UpdateCommand buildUpdateCommand(Context context, ContentValues cv, String where,
                                                   String[] where_args) {
        return new UpdateCommand(context, ContactEntry.TABLE_NAME, cv, where, where_args);
    }
}

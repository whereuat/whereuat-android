package xyz.whereuat.whereuat.utils;

import android.content.ContentValues;
import android.content.Context;

import xyz.whereuat.whereuat.db.command.DeleteCommand;
import xyz.whereuat.whereuat.db.command.InsertCommand;
import xyz.whereuat.whereuat.db.command.QueryCommand;
import xyz.whereuat.whereuat.db.entry.ContactRequestEntry;

/**
 * Utility functions for dealing with pending requests.
 */
public class ContactRequestUtils {
    public static InsertCommand buildInsertCommand(Context context, String name, String phone) {
        ContentValues values = new ContentValues();
        values.put(ContactRequestEntry.COLUMN_NAME, name);
        values.put(ContactRequestEntry.COLUMN_PHONE, phone);
        return new InsertCommand(context, ContactRequestEntry.TABLE_NAME, null, values);
    }

    public static QueryCommand buildSelectAllCommand(Context context) {
        return new QueryCommand(context, ContactRequestEntry.TABLE_NAME, false, null, null, null,
                null, null, null, null);
    }

    public static QueryCommand buildSelectByPhoneCommand(Context context, String phone) {
        String where = String.format("%s=?", ContactRequestEntry.COLUMN_PHONE);
        return new QueryCommand(context, ContactRequestEntry.TABLE_NAME, false, null, where,
                new String[] {phone}, null, null, null, null);
    }

    public static DeleteCommand buildDeleteByPhoneCommand(Context context, String phone) {
        String where = String.format("%s=?", ContactRequestEntry.COLUMN_PHONE);
        return new DeleteCommand(context, ContactRequestEntry.TABLE_NAME, where,
                new String[] {phone});
    }
}

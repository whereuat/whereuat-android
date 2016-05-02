package xyz.whereuat.whereuat.utils;

import android.content.ContentValues;
import android.content.Context;

import xyz.whereuat.whereuat.db.command.InsertCommand;
import xyz.whereuat.whereuat.db.command.QueryCommand;
import xyz.whereuat.whereuat.db.command.UpdateCommand;
import xyz.whereuat.whereuat.db.entry.ContactEntry;

/**
 * Utilities for the contacts table of the database
 */
public class ContactUtils {
    private static final String TAG = "ContactObj";

    /**
     * Contact utility class to store information about a contact
     */
    public static class Contact {
        private String mName;
        private String mPhone;

        /**
         * Default constructor for Contact objects
         */
        public Contact() {
            mName = null;
            mPhone = null;
        }

        /**
         * Constructor for Contact objects
         *
         * @param name_ Name of contact
         * @param phone_ Phone number of contact
         */
        public Contact(String name_, String phone_) {
            mName = name_;
            mPhone = phone_;
        }

        /**
         * Getter method for contact name
         *
         * @return Contact's name as String
         */
        public String getName() {
            return mName;
        }

        /**
         * Getter method for contact phone number
         *
         * @return Contact's phone number as String
         */
        public String getPhone() {
            return mPhone;
        }
    }

    /**
     * Method to build a command to insert a contact into the table
     *
     * @param context Context to build the command
     * @param name Name of the contact to be inserted
     * @param phone Phone number of the contact to be inserted
     * @param autoshare Autoshare status of the contact to be inserted
     * @param color ContactCard background color of the contact to be inserted
     * @return InsertCommand to insert the given contact
     */
    public static InsertCommand buildInsertCommand(Context context, String name, String phone,
                                                   boolean autoshare, int color) {
        ContentValues values = new ContentValues();
        values.put(ContactEntry.COLUMN_NAME, name);
        values.put(ContactEntry.COLUMN_PHONE, phone);
        values.put(ContactEntry.COLUMN_AUTOSHARE, autoshare);
        values.put(ContactEntry.COLUMN_COLOR, color);

        return new InsertCommand(context, ContactEntry.TABLE_NAME, null, values);
    }

    /**
     * Method to build a command to query for a contact by their phone number
     *
     * @param context Context to build the command
     * @param phone Phone number of the contact to be queried
     * @param select_cols Columns to be included in the query's result
     * @return QueryCommand to query for the given phone number
     */
    public static QueryCommand buildSelectContactByPhoneCommand(Context context, String phone,
                                                                String[] select_cols) {
        String selection = String.format("%s=?", ContactEntry.COLUMN_PHONE);
        return new QueryCommand(context, ContactEntry.TABLE_NAME, true, select_cols, selection,
                new String[] {phone}, null, null, null, null);
    }

    /**
     * Method to build a command to select all contacts
     *
     * @param context Context to build the command
     * @param select_cols Columns to be included in the query's result
     * @return QueryCommand to query over all of the contacts
     */
    public static QueryCommand buildSelectAllCommand(Context context, String[] select_cols) {
        return new QueryCommand(context, ContactEntry.TABLE_NAME, false, select_cols, null, null,
                                null, null, null, null);
    }

    /**
     * Method to build a command to update a specific contact
     *
     * @param context Context to build the command
     * @param cv Values to update the entry with
     * @param where SQL WHERE clause
     * @param where_args Arguments to the WHERE clause
     * @return UpdateCommand to perform the given table update
     */
    public static UpdateCommand buildUpdateCommand(Context context, ContentValues cv, String where,
                                                   String[] where_args) {
        return new UpdateCommand(context, ContactEntry.TABLE_NAME, cv, where, where_args);
    }
}

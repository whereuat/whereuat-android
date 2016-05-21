package xyz.whereuat.whereuat.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;

import java.util.Random;

import java.util.Arrays;
import java.util.LinkedList;

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

    public static int generateRandomColor() {
        Random rnd = new Random();
        // A number between 0 and 360.
        float hue = rnd.nextInt(360);
        // The multiplier keeps the value in a range, the addition keeps the number farther from 0
        // so colors that are almost black aren't generated.
        float value = rnd.nextFloat() * 0.4f + 0.4f;
        // Keep the saturation constant at 0.3.
        return Color.HSVToColor(new float[] {hue, 0.3f, value});
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

    public static QueryCommand buildSelectContactByIdCommand(Context context, String id,
                                                             String[] select_cols) {
        String selection = String.format("%s=?", ContactEntry._ID);
        return new QueryCommand(context, ContactEntry.TABLE_NAME, true, select_cols, selection,
                new String[] {id}, null, null, null, null);
    }

    /**
     * Method to build a command to select all contacts
     *
     * @param context Context to build the command
     * @return QueryCommand to query over all of the contacts
     */
    public static QueryCommand buildSelectAllCommand(Context context) {
        return new QueryCommand(context, ContactEntry.TABLE_NAME, false, ContactEntry.COLUMNS, null,
                null, null, null, null, null);
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

    /**
     * Method to get parse initials from a name
     *
     * @param name Name to parse initials from
     * @return String containing the first and last initials of the name
     */
    public static String getInitials(String name) {
        final LinkedList<String> prefixes = new LinkedList<>(Arrays.asList("ms", "miss", "mrs",
                "mr", "rev", "fr", "dr", "doctor", "atty", "prof", "professor", "hon", "pres",
                "gov", "coach", "ofc", "officer", "sir"));
        final LinkedList<String> suffixes = new LinkedList<>(Arrays.asList("cpa", "md", "esq", "jr",
                "sr", "phd", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x", "xi",
                "xii", "xiii", "xiv", "xv"));
        String initials = "";
        String cleaned_name = name.toLowerCase().replaceAll("\\.", "");
        LinkedList<String> split_name = new LinkedList<>(Arrays.asList(cleaned_name.split(" ")));
        while (split_name.size() > 0 && prefixes.contains(split_name.getFirst())) {
            split_name.removeFirst();
        }
        while(split_name.size() > 0 && suffixes.contains(split_name.getLast())) {
            split_name.removeLast();
        }

        if (split_name.size() > 0) {
                initials += split_name.getFirst().charAt(0);
            if (split_name.size() > 1) {
                initials += split_name.getLast().charAt(0);
            }
        }
        return initials.toUpperCase();
    }
}

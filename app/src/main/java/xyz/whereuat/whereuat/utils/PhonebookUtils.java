package xyz.whereuat.whereuat.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import xyz.whereuat.whereuat.Constants;

/**
 * Created by kangp3 on 4/29/16.
 */
public class PhonebookUtils {
    private static final String TAG = "PhonebookUtil";

    /**
     * Helper to retrieve a contact's information from the phone book activity
     *
     * @param data The Intent carrying the data of the phone book selection
     * @param context Context to get content resolver from
     * @return Contact object storing the name and phone number of the retrieved contact
     */
    public static ContactUtils.Contact getContactFromPhonebook(Intent data, Context context) {
        // Get the data from the Intent
        Uri contact_data = data.getData();

        // Query the Intent's data and extract the ID and name of the contact
        String[] projection = {Phone.DISPLAY_NAME, Phone.NUMBER};
        String sel = String.format("%s = %s", Phone.TYPE, Phone.TYPE_MOBILE);
        Cursor c =  context.getContentResolver().query(contact_data, projection, sel, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                String name = c.getString(c.getColumnIndex(Phone.DISPLAY_NAME));
                String phone = convertToE164(c.getString(c.getColumnIndex(Phone.NUMBER)));

                return new ContactUtils.Contact(name, phone);
            }
            c.close();
        }
        return new ContactUtils.Contact();
    }

    /**
     * Helper method to convert a phone number to E164 format as expected by the server
     * (+1XXXXXXXXXX)
     *
     * @param raw_phone_number The unformatted phone number as stored by the system contacts
     *                         application
     * @return String storing the E164-formatted phone number
     */
    public static String convertToE164(String raw_phone_number) {
        PhoneNumberUtil phone_util = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phone_proto = phone_util.parse(raw_phone_number,
                    Constants.DEFAULT_PHONE_REGION);
            if (phone_util.isValidNumber(phone_proto)) {
                return phone_util.format(phone_proto, PhoneNumberUtil.PhoneNumberFormat.E164);
            } else {
                Log.d(TAG, "Invalid number");
                return null;
            }
        } catch (NumberParseException e) {
            Log.d(TAG, "Error parsing phone number");
            return null;
        }
    }
}

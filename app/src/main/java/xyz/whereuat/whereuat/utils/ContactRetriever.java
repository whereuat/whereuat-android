package xyz.whereuat.whereuat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import xyz.whereuat.whereuat.Constants;

/**
 * Utility class to retrieve contacts from the system contacts application
 */
public class ContactRetriever {
    private static Context mContext;
    private String mPhoneNum;
    private String mContactID;
    private String mContactName;
    private static final String[] PROJECTION = {
        Contacts._ID,
        Contacts.DISPLAY_NAME
    };

    private static final String TAG = "ContactRtrv";

    /**
     * Constructor for the ContactRetriever. This initiates the system contacts application and
     * retrieves the data about the chosen contact, storing their name, ID, and phone number in
     * the corresponding member variables.
     *
     * @param req_code Request code of the activity start request
     * @param result_code Result code specifying the success of the activity
     * @param data Intent that carries the result data
     * @param con Context for the activity
     */
    public ContactRetriever(int req_code, int result_code, Intent data, Context con) {
        mContext = con;
        switch (req_code) {
            // Contact retriever activity request from MainActivity
            case (1) :
                if (result_code == Activity.RESULT_OK) {
                    // Get the data from the Intent
                    Uri contactData = data.getData();

                    // Query the Intent's data and extract the ID and name of the contact
                    Cursor c =  con.getContentResolver().query(contactData, PROJECTION, null, null,
                            null);
                    if (c != null) {
                        if (c.moveToFirst()) {
                            mContactID = c.getString(c.getColumnIndex(Contacts._ID));
                            mContactName = c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME));
                        }
                        c.close();
                    }

                    String contact_query = String.format("%s = ? AND %s = %s",
                            Phone.CONTACT_ID,
                            Phone.TYPE,
                            Phone.TYPE_MOBILE);

                    // Get the contact's phone number and convert it to E164 format
                    Cursor cursorPhone = con.getContentResolver().query(Phone.CONTENT_URI,
                            new String[]{Phone.NUMBER},
                            contact_query,
                            new String[]{mContactID},
                            null);

                    if (cursorPhone != null) {
                        if (cursorPhone.moveToFirst()) {
                            int col_index = cursorPhone.getColumnIndex(Phone.NUMBER);
                            String raw_phone_number = cursorPhone.getString(col_index);

                            mPhoneNum = convertToE164(raw_phone_number);
                        }
                        cursorPhone.close();
                    }
                }
                break;
        }
    }

    /**
     * Helper method to convert a phone number to E164 format as expected by the server
     * (+1XXXXXXXXXX)
     *
     * @param raw_phone_number The unformatted phone number as stored by the system contacts
     *                         application
     * @return String storing the E164-formatted phone number
     */
    private String convertToE164(String raw_phone_number) {
        PhoneNumberUtil phone_util = PhoneNumberUtil.getInstance();
        try {
            PhoneNumber phone_proto = phone_util.parse(raw_phone_number,
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

    /**
     * Getter for contact's ID
     *
     * @return The contact's system ID as a String
     */
    public String getContactID(){
        return mContactID;
    }

    /**
     * Getter for contact's name
     *
     * @return The contact's name as a String
     */
    public String getContactName(){
        return mContactName;
    }

    /**
     * Getter for contact's phone number
     *
     * @return The contact's phone number as a String
     */
    public String getPhoneNumber(){
        return mPhoneNum;
    }
}

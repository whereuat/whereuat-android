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
 * Created by whites5 on 3/22/16.
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


    public ContactRetriever(int reqCode, int resultCode, Intent data, Context con) {
        mContext = con;
        switch (reqCode) {
            case (1) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();

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

    public String getContactID(){
        return mContactID;
    }

    public String getContactName(){
        return mContactName;
    }

    public String getPhoneNumber(){
        return mPhoneNum;
    }
}

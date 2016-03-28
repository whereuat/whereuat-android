package com.whereuat.whereu;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.content.Context;

/**
 * Created by whites5 on 3/22/16.
 */
public class ContactRetriever {
    private static Context mContext;
    private static String phone_num;
    private String contactID;
    private String contactName;
    private static final String[] projection = {
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME
    };


    public ContactRetriever(int reqCode, int resultCode, Intent data, Context con) {
        mContext = con;
        switch (reqCode) {
            case (1) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();

                    Cursor c =  con.getContentResolver().query(contactData, projection, null, null, null);
                    if (c.moveToFirst()) {
                        contactID = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                        contactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    }
                    c.close();

                    Cursor cursorPhone = con.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                            new String[]{contactID},
                            null);

                    if (cursorPhone.moveToFirst()) {
                        phone_num = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }

                    cursorPhone.close();
                }
                break;
        }
    }

    public String getContactID(){
        return contactID;
    }

    public String getContactName(){
        return contactName;
    }

    public String getPhoneNumber(){
        return phone_num;
    }
}

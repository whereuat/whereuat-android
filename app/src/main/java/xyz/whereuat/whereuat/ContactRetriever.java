package xyz.whereuat.whereuat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;

/**
 * Created by whites5 on 3/22/16.
 */
public class ContactRetriever {
    private static Context mContext;
    private String mPhoneNum;
    private String mContactID;
    private String mContactName;
    private static final String[] PROJECTION = {
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME
    };


    public ContactRetriever(int reqCode, int resultCode, Intent data, Context con) {
        mContext = con;
        switch (reqCode) {
            case (1) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();

                    Cursor c =  con.getContentResolver().query(contactData, PROJECTION, null, null, null);
                    if (c.moveToFirst()) {
                        mContactID = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                        mContactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    }
                    c.close();

                    String contact_query = String.format("%s = ? AND %s = %s",
                            CommonDataKinds.Phone.CONTACT_ID,
                            CommonDataKinds.Phone.TYPE,
                            CommonDataKinds.Phone.TYPE_MOBILE);

                    Cursor cursorPhone = con.getContentResolver().query(CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{CommonDataKinds.Phone.NUMBER},
                            contact_query,
                            new String[]{mContactID},
                            null);

                    if (cursorPhone.moveToFirst()) {
                        mPhoneNum = cursorPhone.getString(cursorPhone.getColumnIndex(CommonDataKinds.Phone.NUMBER));
                    }

                    cursorPhone.close();
                }
                break;
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

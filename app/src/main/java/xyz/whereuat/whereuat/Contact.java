package xyz.whereuat.whereuat;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import xyz.whereuat.whereuat.db.DbObject;
import xyz.whereuat.whereuat.db.DbTask;
import xyz.whereuat.whereuat.db.command.InsertCommand;
import xyz.whereuat.whereuat.db.entry.ContactEntry;

/**
 * Created by kangp3 on 4/10/16.
 */
public class Contact implements DbObject {
    public final String name;
    public final String phone;
    public final boolean autoshare;

    private static final String TAG = "KeyLocObj";

    public Contact(String _name, String _phone, boolean _autoshare) {
        name = _name;
        phone = _phone;
        autoshare = _autoshare;
    }

    public void dbInsert(Context context) {
        ContentValues values = new ContentValues();
        values.put(ContactEntry.COLUMN_NAME, name);
        values.put(ContactEntry.COLUMN_PHONE, phone);
        values.put(ContactEntry.COLUMN_AUTOSHARE, autoshare);

        InsertCommand cmd = new InsertCommand(context, ContactEntry.TABLE_NAME, null, values);
        new DbTask(new DbTask.AsyncResponse() {
            @Override
            public void processFinish(Object result) {
                if ((Long) result != -1) {
                    Log.d(TAG, "Successfully inserted");
                } else {
                    Log.d(TAG, "Some weird things happened when inserting into DB");
                }
            }
        }).execute(cmd);
    }
}

/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.whereuat.whereuat.gcm;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import xyz.whereuat.whereuat.AsyncExecutor;
import xyz.whereuat.whereuat.Constants;
import xyz.whereuat.whereuat.db.entry.ContactEntry;
import xyz.whereuat.whereuat.utils.ContactUtils;
import xyz.whereuat.whereuat.utils.NotificationUtils;

/**
 * This class is responsible for receiving push notifications and forming them into notifications to
 * be displayed to the user.
 */
public class WuaGcmListenerService extends GcmListenerService {
    private static final String TAG = "WuaGcmListenerService";

    /**
     * This function is called when a push notification is received and determines what type of
     * GCM message was received and handles each case.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (isAtRequest(data)) {
            handleAtRequest(data);
        } else if (isAtResponse(data)) {
            NotificationUtils.sendAtResponseNotification(this,
                    data.getString(Constants.GCM_FROM_KEY),
                    data.getString(Constants.GCM_PLACE_KEY));
        } else
            Log.d(TAG, "Bad notification received.");
    }

    /**
     * Determines how to handle an @request based on if the sender is autoshared or not. If they are
     * autoshared, then an intent is sent to MainActivity.AtResponseInitiateReceiver to send an
     * AtResponse. If they aren't autoshared, a notification is displayed with a button to respond.
     *
     * @param data a Bundle of data received with the notification which includes the sender's phone
     *             number
     */
    private void handleAtRequest(final Bundle data) {
        // Build and execute a query to determine if the contact is autoshared.
        AsyncExecutor.service.submit(new Runnable() {
            @Override
            public void run() {
                String from_phone = data.getString(Constants.GCM_FROM_KEY);
                Cursor contact = ContactUtils.buildSelectContactByPhoneCommand(
                        WuaGcmListenerService.this, from_phone,
                        new String[] {ContactEntry.COLUMN_AUTOSHARE}).call();
                if (contact.moveToFirst()) {
                    boolean is_autoshared = contact.getInt(
                            contact.getColumnIndex(ContactEntry.COLUMN_AUTOSHARE)) > 0;
                    // If the contact is autoshared, send an intent to the main activity to send an
                    // @response without displaying the notification.
                    if (is_autoshared) {
                        Intent intent = new Intent(Constants.AT_RESPONSE_INITIATE_BROADCAST);
                        intent.putExtra(Constants.TO_PHONE_EXTRA, from_phone);
                        sendBroadcast(intent);
                    } else {
                        NotificationUtils.sendAtRequestNotification(WuaGcmListenerService.this,
                                from_phone);
                    }
                } else {
                    Log.d(TAG, "The client was not found.");
                }
            }
        });
    }

    private boolean isAtRequest(Bundle data) {
        return data.getString(Constants.GCM_FROM_KEY) != null &&
                data.getString(Constants.GCM_PLACE_KEY) == null;
    }

    private boolean isAtResponse(Bundle data) {
        return data.getString(Constants.GCM_FROM_KEY) != null &&
                data.getString(Constants.GCM_PLACE_KEY) != null;
    }
}

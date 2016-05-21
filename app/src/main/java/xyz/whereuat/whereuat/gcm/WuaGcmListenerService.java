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
import xyz.whereuat.whereuat.ContactRequestsActivity;
import xyz.whereuat.whereuat.db.entry.ContactEntry;
import xyz.whereuat.whereuat.utils.ContactUtils;
import xyz.whereuat.whereuat.utils.KeyLocationUtils;
import xyz.whereuat.whereuat.utils.NotificationUtils;
import xyz.whereuat.whereuat.utils.ContactRequestUtils;
import xyz.whereuat.whereuat.utils.PhonebookUtils;

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
            KeyLocationUtils.KeyLocation loc = new KeyLocationUtils.KeyLocation(
                    data.getString(Constants.GCM_PLACE_KEY),
                    Double.parseDouble(data.getString(Constants.GCM_LAT_KEY)),
                    Double.parseDouble(data.getString(Constants.GCM_LNG_KEY)));
            NotificationUtils.sendAtResponseNotification(this,
                    data.getString(Constants.GCM_FROM_KEY), loc);
        } else
            Log.d(TAG, "Bad notification received.");
    }

    /**
     * Tries to find the number in the user's whereu@ contacts. If they are found, the user is sent
     * an AtRequest or automatically replied to. If they are not found and haven't sent an AtRequest
     * to the user before, a notification is shown and they are added to the contact requests table.
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
                // Check if the contact is already in the app.
                if (contact.moveToFirst()) {
                    handleKnownContact(contact);
                } else {
                    handleUnknownContact(from_phone);
                }
            }
        });
    }

    /**
     * Check if the contact is autoshared. If they are, automatically send them a response. If they
     * are not, create show a notification.
     *
     * @param contact A cursor to the contact that sent the request.
     */
    private void handleKnownContact(Cursor contact) {
        String from_phone = contact.getString(
                contact.getColumnIndex(ContactEntry.COLUMN_PHONE));
        boolean is_autoshared = contact.getInt(
                contact.getColumnIndex(ContactEntry.COLUMN_AUTOSHARE)) > 0;
        // If the contact is autoshared, send an intent to the main activity to send
        // an AtResponse without displaying the notification.
        if (is_autoshared) {
            Intent intent = new Intent(Constants.AT_RESPONSE_INITIATE_BROADCAST);
            intent.putExtra(Constants.TO_PHONE_EXTRA, from_phone);
            sendBroadcast(intent);
        } else {
            NotificationUtils.sendAtRequestNotification(WuaGcmListenerService.this, from_phone);
        }
    }

    /**
     * Check if the number is in the contact requests table. If they are, it means they have sent a
     * request before so nothing should be done. If they are not, this is the first request they
     * have sent to the user so query the phonebook for their name and add them to the pending
     * requests table.
     *
     * Note: This function runs database commands to it should not be run on the UI thread.
     *
     * @param from_phone The phone number that sent the initial request.
     */
    private void handleUnknownContact(String from_phone) {
        boolean is_recognized = ContactRequestUtils.buildSelectByPhoneCommand(
                WuaGcmListenerService.this, from_phone).call().moveToFirst();
        if (!is_recognized) {
            Log.d(TAG, "Contact is not recognized.");
            // Try to find the contact's name in phonebook based on the phone number. If it's not
            // there, an empty string is used as their name.
            String name = PhonebookUtils.queryPhonebookForContactName(
                    WuaGcmListenerService.this, from_phone);
            boolean was_inserted = ContactRequestUtils.buildInsertCommand(
                    WuaGcmListenerService.this, name, from_phone).call() != -1;

            NotificationUtils.sendPendingRequestNotification(WuaGcmListenerService.this,
                    from_phone);

            if (was_inserted) {
                ContactRequestsActivity.notifyOfContactRequestChange(WuaGcmListenerService.this);
                Log.d(TAG, "Contact was added to contact requests.");
            } else {
                Log.d(TAG, "Contact was not added to contact requests :(");
            }
        }
    }

    private boolean isAtRequest(Bundle data) {
        return data.getString(Constants.GCM_TYPE_KEY).equals(Constants.GCM_AT_REQUEST_TYPE);
    }

    private boolean isAtResponse(Bundle data) {
        return data.getString(Constants.GCM_TYPE_KEY).equals(Constants.GCM_AT_RESPONSE_TYPE);
    }
}

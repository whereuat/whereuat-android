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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.Random;

import xyz.whereuat.whereuat.Constants;
import xyz.whereuat.whereuat.utils.ContactUtils;
import xyz.whereuat.whereuat.MainActivity;
import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.db.DbTask;
import xyz.whereuat.whereuat.db.command.QueryCommand;
import xyz.whereuat.whereuat.db.entry.ContactEntry;

/**
 * This class is responsible for receiving push notifications and forming them into notifications to
 * be displayed to the user.
 */
public class WuaGcmListenerService extends GcmListenerService {
    private static final String TAG = "WuaGcmListenerService";

    /**
     * Called when a push notification is received.
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
            sendAtResponseNotification(data.getString(Constants.GCM_FROM_KEY),
                    data.getString(Constants.GCM_PLACE_KEY));
        } else
            Log.d(TAG, "Bad notification received.");
    }

    /**
     * Determines how to handle an @request based on if the sender is autoshared or not. If they are
     * autoshared, then an intent is sent to MainActivity.AtResponseInitiateReceiver to send an
     * @response. If they aren't autoshared, a notification is displayed with a button to respond.
     *
     * @param data a Bundle of data received with the notification which includes the sender's phone
     *             number
     */
    private void handleAtRequest(Bundle data) {
        final String from_phone = data.getString(Constants.GCM_FROM_KEY);
        // Build and execute a query to determine if the contact is autoshared.
        QueryCommand query = ContactUtils.buildSelectContactByPhoneCommand(this, from_phone,
                new String[] {ContactEntry.COLUMN_AUTOSHARE});
        new DbTask() {
            @Override
            public void onPostExecute(Object result) {
                Cursor c = (Cursor) result;
                if (c.moveToFirst()) {
                    boolean is_autoshared = c.getInt(
                            c.getColumnIndex(ContactEntry.COLUMN_AUTOSHARE)) > 0;
                    // If the contact is autoshared, send an intent to the main activity to send an
                    // @response without displaying the notification.
                    if (is_autoshared) {
                        Intent intent = new Intent(Constants.AT_RESPONSE_INITIATE_BROADCAST);
                        intent.putExtra(Constants.TO_PHONE_EXTRA, from_phone);
                        sendBroadcast(intent);
                    } else {
                        sendAtRequestNotification(from_phone);
                    }
                } else {
                    Log.d(TAG, "The client was not found.");
                }
            }
        }.execute(query);
    }

    private boolean isAtRequest(Bundle data) {
        return data.getString(Constants.GCM_FROM_KEY) != null &&
                data.getString(Constants.GCM_PLACE_KEY) == null;
    }

    private boolean isAtResponse(Bundle data) {
        return data.getString(Constants.GCM_FROM_KEY) != null &&
                data.getString(Constants.GCM_PLACE_KEY) != null;
    }

    /*

     */

    /**
     * A helper function to send a notification without an action.
     *
     * @param notification_id the integer id to give the notification
     * @param message the message to be displayed in the notification
     */
    private void sendNotification(int notification_id, String message) {
        sendNotification(notification_id, message, null);
    }

    /**
     * Builds a notification and displays it.
     *
     * @param notification_id the integer id to give the notification
     * @param message the message to be displayed in the notification
     * @param action the action to be applied to the notification
     */
    private void sendNotification(int notification_id,
                                  String message, NotificationCompat.Action action) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending_intent = PendingIntent.getActivity(this, notification_id, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notification_builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pending_intent);

        if (action != null)
            notification_builder.addAction(action);

        NotificationManager notification_manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification_manager.notify(notification_id, notification_builder.build());
    }

    /**
     * Get the requester's name and then create a notification saying they sent an @request.
     *
     * @param from_phone a String of the phone number of the requester
     */
    private void sendAtRequestNotification(final String from_phone) {
        // Find the contact in the database with the same phone number so its name can be displayed
        // in the notification.
        QueryCommand query = ContactUtils.buildSelectContactByPhoneCommand(this, from_phone,
                new String[] {ContactEntry.COLUMN_NAME});
        new DbTask() {
            @Override
            public void onPostExecute(Object result) {
                Cursor c = (Cursor) result;
                if (c.moveToFirst()) {
                    // Extract the contact's name and use it in the notification's message.
                    String name = c.getString(c.getColumnIndex(ContactEntry.COLUMN_NAME));
                    int notification_id = (new Random()).nextInt(Integer.MAX_VALUE);
                    sendNotification(notification_id, String.format("%s: whereu@?", name),
                            createResponseAction(notification_id, from_phone));
                } else {
                    Log.d(TAG, "Couldn't retrieve the client from the db.");
                }
            }
        }.execute(query);
    }

    /**
     * Get the requestee's name and then create a notification saying where they are at.
     *
     * @param from_phone a String of the phone number of the requestee
     * @param loc a String of the name of the location
     */
    private void sendAtResponseNotification(String from_phone, final String loc) {
        QueryCommand query = ContactUtils.buildSelectContactByPhoneCommand(this, from_phone,
                new String[] {ContactEntry.COLUMN_NAME});
        // Once the query has completed, create the notification.
        new DbTask() {
            @Override
            public void onPostExecute(Object result) {
                Cursor c = (Cursor) result;
                if (c.moveToFirst()) {
                    String name = c.getString(c.getColumnIndex(ContactEntry.COLUMN_NAME));
                    sendNotification((new Random()).nextInt(Integer.MAX_VALUE),
                            String.format("%s is @ %s", name, loc));
                } else {
                    Log.d(TAG, "Couldn't retrieve the client from the db.");
                }
            }
        }.execute(query);
    }

    /**
     * Creates an action for an @response that sends an intent to the
     * {@link xyz.whereuat.whereuat.AtResponseInitiateReceiver} which will send a POST.
     *
     * @param notification_id
     * @param to_phone
     * @return
     */
    private NotificationCompat.Action createResponseAction(int notification_id, String to_phone) {
        // Create the intent that will go to the receiver.
        Intent intent = new Intent(Constants.AT_RESPONSE_INITIATE_BROADCAST);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.NOTIFICATION_ID_EXTRA, notification_id);
        intent.putExtra(Constants.TO_PHONE_EXTRA, to_phone);

        // Create the intent that will allow the system to show the notification.
        PendingIntent pending_intent = PendingIntent.getBroadcast(this, notification_id, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Action.Builder action_builder =
                new NotificationCompat.Action.Builder(R.drawable.ic_stat_ic_notification, "At",
                        pending_intent);

        return action_builder.build();
    }
}

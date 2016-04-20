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
import xyz.whereuat.whereuat.ContactUtils;
import xyz.whereuat.whereuat.MainActivity;
import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.db.DbTask;
import xyz.whereuat.whereuat.db.command.QueryCommand;
import xyz.whereuat.whereuat.db.entry.ContactEntry;

public class WuaGcmListenerService extends GcmListenerService {
    private static final String TAG = "WuaGcmListenerService";

    /**
     * Called when message is received.
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

    /*
        Determines how to handle an @request based on if the sender is autoshared or not. If they
        are autoshared, then an intent is sent to MainActivity.AtResponseInitiateReceiver to send an
        @response. If they aren't autoshared, a notification is displayed with a button to respond.
     */
    private void handleAtRequest(Bundle data) {
        final String from_phone = data.getString(Constants.GCM_FROM_KEY);
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
                    // @response.
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
        A helper function to send a notification without an action.
     */
    private void sendNotification(int notification_id, String message) {
        sendNotification(notification_id, message, null);
    }

    private void sendNotification(int notification_id,
                                  String message, NotificationCompat.Action action) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending_intent = PendingIntent.getActivity(this, notification_id, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri sound_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notification_builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle("whereu@")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(sound_uri)
                .setContentIntent(pending_intent);

        if (action != null)
            notification_builder.addAction(action);

        NotificationManager notification_manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification_manager.notify(notification_id, notification_builder.build());
    }

    private void sendAtRequestNotification(final String from_phone) {
        QueryCommand query = ContactUtils.buildSelectContactByPhoneCommand(this, from_phone,
                new String[] {ContactEntry.COLUMN_NAME});
        new DbTask() {
            @Override
            public void onPostExecute(Object result) {
                Cursor c = (Cursor) result;
                if (c.moveToFirst()) {
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

    private void sendAtResponseNotification(String from_phone, final String loc) {
        QueryCommand query = ContactUtils.buildSelectContactByPhoneCommand(this, from_phone,
                new String[] {ContactEntry.COLUMN_NAME});
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

    private NotificationCompat.Action createResponseAction(int notification_id, String to_phone) {
        Intent intent = new Intent(Constants.AT_RESPONSE_INITIATE_BROADCAST);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.NOTIFICATION_ID_EXTRA, notification_id);
        intent.putExtra(Constants.TO_PHONE_EXTRA, to_phone);

        PendingIntent pending_intent = PendingIntent.getBroadcast(this, notification_id, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Action.Builder action_builder =
                new NotificationCompat.Action.Builder(R.drawable.ic_stat_ic_notification, "At",
                        pending_intent);

        return action_builder.build();
    }
}

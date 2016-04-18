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
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.Random;

import xyz.whereuat.whereuat.Constants;
import xyz.whereuat.whereuat.MainActivity;
import xyz.whereuat.whereuat.R;

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
            sendAtRequestNotification(data.getString(Constants.GCM_FROM_KEY));
        } else if (isAtResponse(data)) {
            sendAtResponseNotification(data.getString(Constants.GCM_FROM_KEY),
                    data.getString(Constants.GCM_PLACE_KEY));
        } else
            Log.d(TAG, "Bad notification received.");
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

    private void sendAtRequestNotification(String from_phone) {
        int notification_id = (new Random()).nextInt(Integer.MAX_VALUE);
        sendNotification(notification_id, String.format("%s: whereu@?", from_phone),
                createResponseAction(notification_id, from_phone));
    }

    private void sendAtResponseNotification(String from_phone, String loc) {
        sendNotification((new Random()).nextInt(Integer.MAX_VALUE),
                String.format("%s is @ %s", from_phone, loc));
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

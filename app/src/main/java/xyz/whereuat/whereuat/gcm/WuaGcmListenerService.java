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
        sendNotification(String.format("%s: whereu@?", data.getString("from-#")));
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        // Generate a random integer so the notification can be uniquely identified later.
        int notification_id = (new Random()).nextInt(Integer.MAX_VALUE);

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
                .addAction(createResponseAction(notification_id))
                .setContentIntent(pending_intent);

        NotificationManager notification_manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification_manager.notify(notification_id, notification_builder.build());
    }

    private NotificationCompat.Action createResponseAction(int notification_id) {
        Intent intent = new Intent(Constants.AT_RESPOND_BROADCAST);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.NOTIFICATION_ID_EXTRA, notification_id);

        PendingIntent pending_intent = PendingIntent.getBroadcast(this, notification_id, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Action.Builder action_builder =
                new NotificationCompat.Action.Builder(R.drawable.ic_stat_ic_notification, "At",
                        pending_intent);

        return action_builder.build();
    }
}

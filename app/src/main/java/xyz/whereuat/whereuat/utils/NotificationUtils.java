package xyz.whereuat.whereuat.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Random;

import xyz.whereuat.whereuat.AsyncExecutor;
import xyz.whereuat.whereuat.Constants;
import xyz.whereuat.whereuat.ContactRequestsActivity;
import xyz.whereuat.whereuat.MainActivity;
import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.db.entry.ContactEntry;

/**
 * This is a class for helping to send notifications.
 */
public class NotificationUtils {
    private static final String TAG = "NotificationUtils";

    /**
     * Get the requester's name and then create a notification saying they sent an @request.
     *
     * @param context the context to send the notification from
     * @param from_phone a String of the phone number of the requester
     */
    public static void sendAtRequestNotification(final Context context, final String from_phone) {
        // Find the contact in the database with the same phone number so its name can be displayed
        // in the notification.
        AsyncExecutor.service.submit(new Runnable() {
            @Override
            public void run() {
                Cursor c = ContactUtils.buildSelectContactByPhoneCommand(context, from_phone,
                        new String[] {ContactEntry.COLUMN_NAME}).call();
                if (c.moveToFirst()) {
                    // Extract the contact's name and use it in the notification's message.
                    String name = c.getString(c.getColumnIndex(ContactEntry.COLUMN_NAME));
                    int notification_id = (new Random()).nextInt(Integer.MAX_VALUE);
                    sendNotification(context, MainActivity.class, notification_id,
                            String.format("%s: whereu@?", name), false,
                            createAtRequestAction(context, notification_id, from_phone));
                } else {
                    Log.d(TAG, "Couldn't retrieve the client from the db.");
                }
            }
        });
    }

    /**
     * Get the requestee's name and then create a notification saying where they are at.
     *
     * @param from_phone a String of the phone number of the requestee
     * @param loc a String of the name of the location
     */
    public static void sendAtResponseNotification(final Context context, final String from_phone,
                                                  final String loc) {
        // Once the query has completed, create the notification.
        AsyncExecutor.service.submit(new Runnable() {
            @Override
            public void run() {
                Cursor c = ContactUtils.buildSelectContactByPhoneCommand(context, from_phone,
                        new String[] {ContactEntry.COLUMN_NAME}).call();
                if (c.moveToFirst()) {
                    String name = c.getString(c.getColumnIndex(ContactEntry.COLUMN_NAME));
                    sendNotification(context, MainActivity.class,
                            (new Random()).nextInt(Integer.MAX_VALUE),
                            String.format("%s is @ %s", name, loc), true, null);
                } else {
                    Log.d(TAG, "Couldn't retrieve the client from the db.");
                }
            }
        });
    }

    /**
     * Send a notification that shows that an unknown number sent the client an AtRequest. On click,
     * the notification redirects to the contact requests activity.
     *
     * @param context the context that triggered the notification
     * @param from_phone the number that sent the request
     */
    public static void sendPendingRequestNotification(Context context, String from_phone) {
        sendNotification(context, ContactRequestsActivity.class,
                (new Random()).nextInt(Integer.MAX_VALUE),
                String.format("%s: whereu@?", from_phone), true, null);
    }

    /**
     * Sends a notification to the Android system.
     *
     * @param context the context that the notification is sent from
     * @param activity_class the activity that should be opened when the notification is clicked
     * @param notification_id the id of the notification, which should match the id in |action|
     * @param message the text in the notification
     * @param action the actions that should be set on the notification
     */
    private static void sendNotification(Context context, Class<?> activity_class,
                                         int notification_id, String message,
                                         boolean should_auto_cancel,
                                         NotificationCompat.Action action) {
        Intent intent = new Intent(context, activity_class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending_intent = PendingIntent.getActivity(context, notification_id, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notification_builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(should_auto_cancel)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pending_intent);

        if (action != null)
            notification_builder.addAction(action);

        NotificationManager notification_manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification_manager.notify(notification_id, notification_builder.build());
    }

    /**
     * Creates an action for an @response that sends an intent to the
     * {@link xyz.whereuat.whereuat.AtResponseInitiateReceiver} which will send a POST.
     *
     * @param context the context used to create the PendingIntent that will be attached to the
     *                action
     * @param notification_id the id of the notification that this action will be attached to. The
     *                        id must be added to the action so it can be used to delete the
     *                        notification when the action is resolved.
     * @param to_phone the phone number to be added to the action so it can be resolved
     * @return an Action to be added to a notification
     */
    private static NotificationCompat.Action createAtRequestAction(Context context,
                                                                   int notification_id,
                                                                   String to_phone) {
        // Create the intent that will go to the receiver.
        Intent intent = new Intent(Constants.AT_RESPONSE_INITIATE_BROADCAST);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.NOTIFICATION_ID_EXTRA, notification_id);
        intent.putExtra(Constants.TO_PHONE_EXTRA, to_phone);

        // Create the intent that will allow the system to show the notification.
        PendingIntent pending_intent = PendingIntent.getBroadcast(context, notification_id, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Action.Builder action_builder =
                new NotificationCompat.Action.Builder(R.drawable.ic_stat_ic_notification, "At",
                        pending_intent);

        return action_builder.build();
    }
}

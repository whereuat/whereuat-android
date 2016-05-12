package xyz.whereuat.whereuat;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import xyz.whereuat.whereuat.db.entry.KeyLocationEntry;
import xyz.whereuat.whereuat.utils.HttpRequestHandler;
import xyz.whereuat.whereuat.utils.KeyLocationUtils;
import xyz.whereuat.whereuat.utils.LocationProviderService;
import xyz.whereuat.whereuat.utils.PreferenceController;


/**
 * This class is for receiving broadcasts from the service that handles incoming GCM
 * notifications. It is responsible for sending @responses.
 */
public class AtResponseInitiateReceiver extends BroadcastReceiver {
    private final String TAG = "AtResponseInitReceiver";
    @Override
    public void onReceive(final Context context, final Intent intent) {
        // If there is a notification associated with this intent it should be canceled.
        if (intent.hasExtra(Constants.NOTIFICATION_ID_EXTRA)) {
            NotificationManager nm = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            nm.cancel(intent.getIntExtra(Constants.NOTIFICATION_ID_EXTRA, 0));
        }

        AsyncExecutor.service.submit(new Runnable() {
            @Override
            public void run() {
                Location loc = LocationProviderService.getLocation();

                String[] select_cols = {KeyLocationEntry.COLUMN_NAME,
                        KeyLocationEntry.COLUMN_LATITUDE, KeyLocationEntry.COLUMN_LONGITUDE};
                Cursor all_locs = KeyLocationUtils.buildSelectAllCommand(context, select_cols)
                        .call();
                KeyLocationUtils.KeyLocation key_loc =
                        all_locs.getCount() == 0 ? new KeyLocationUtils.KeyLocation() :
                                KeyLocationUtils.findNearestLoc(all_locs, loc);
                try {
                    // Post the @response.
                    (new HttpRequestHandler(context)).postAtResponse(
                            (new PreferenceController(context)).getClientPhoneNumber(),
                            intent.getStringExtra(Constants.TO_PHONE_EXTRA), loc.getLatitude(),
                            loc.getLongitude(), key_loc,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d(TAG, "200 from @response POST");
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(final VolleyError error) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String text = "Error sending the location POST " +
                                                    error.toString();
                                            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }
                    );
                } catch (NullPointerException e) {
                    Log.d(TAG, "location object was null");
                }
            }
        });
    }
}

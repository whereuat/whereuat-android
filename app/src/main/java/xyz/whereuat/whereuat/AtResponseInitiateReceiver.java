package xyz.whereuat.whereuat;

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

import xyz.whereuat.whereuat.db.DbTask;
import xyz.whereuat.whereuat.db.command.QueryCommand;
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

        String[] select_cols = {KeyLocationEntry.COLUMN_NAME, KeyLocationEntry.COLUMN_LATITUDE,
                KeyLocationEntry.COLUMN_LONGITUDE};
        QueryCommand query = KeyLocationUtils.buildSelectAllCommand(context, select_cols);
        // Execute the query to select all key locations.
        new DbTask() {
            /**
             * After successfully selecting all the key locations, get the device's current
             * location, get the phone number of the requester, and post an @response.
             * @param result a Cursor going over the data of all the key locations
             */
            @Override
            public void onPostExecute(Object result) {
                Location loc = LocationProviderService.getLocation();
                Cursor c = (Cursor) result;
                KeyLocationUtils.KeyLocation key_loc =
                        c.getCount() == 0 ? new KeyLocationUtils.KeyLocation() :
                                KeyLocationUtils.findNearestLoc(c, loc);
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
                                public void onErrorResponse(VolleyError error) {
                                    String text = "Error sending the location POST " +
                                            error.toString();
                                    Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                                }
                            }
                    );
                } catch (NullPointerException e) {
                    Log.d(TAG, "location object was null");
                }

            }
        }.execute(query);
    }
}

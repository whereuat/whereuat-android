package xyz.whereuat.whereuat;

import android.Manifest;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.clans.fab.FloatingActionMenu;

import java.util.Random;

import xyz.whereuat.whereuat.db.DbTask;
import xyz.whereuat.whereuat.db.command.InsertCommand;
import xyz.whereuat.whereuat.db.command.QueryCommand;
import xyz.whereuat.whereuat.db.entry.ContactEntry;
import xyz.whereuat.whereuat.db.entry.KeyLocationEntry;
import xyz.whereuat.whereuat.ui.ContactCardCursorAdapter;
import xyz.whereuat.whereuat.ui.views.KeyLocDialogFragment;
import xyz.whereuat.whereuat.utils.ContactRetriever;
import xyz.whereuat.whereuat.utils.ContactUtils;
import xyz.whereuat.whereuat.utils.HttpRequestHandler;
import xyz.whereuat.whereuat.utils.KeyLocationUtils;
import xyz.whereuat.whereuat.utils.LocationProviderService;
import xyz.whereuat.whereuat.utils.PreferenceController;

/**
 * This class contains the main contact grid view full of ContactCard squares. It is responsible
 * for loading a cursor with data from the contacts table and for providing buttons for adding key
 * locations and contacts.
 */
public class MainActivity extends AppCompatActivity implements OnScrollListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private FloatingActionMenu mMenu;
    private ContactCardCursorAdapter mAdapter;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMenu = (FloatingActionMenu) findViewById(R.id.menu);
        mAdapter = new ContactCardCursorAdapter(this);

        // Connect the contact GridView into the adapter that holds the data from the database.
        GridView gridview = (GridView)findViewById(R.id.contact_gridview);
        gridview.setAdapter(mAdapter);
        gridview.setOnScrollListener(this);
        getSupportLoaderManager().initLoader(0, null, this);

        String location_permission = Manifest.permission.ACCESS_FINE_LOCATION;

        // Make sure the application explicitly asks the user for location permissions.
        if (ContextCompat.checkSelfPermission(this, location_permission) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, location_permission)) {
                Log.d(TAG, "show permission rationale");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{location_permission},
                        Constants.WHEREUAT_PERMISSION_REQUEST_LOCATION);
                Log.d(TAG, "show request permissions");
            }
        } else {
            Log.d(TAG, "gucci perm");
        }

        // When the activity is created start the LocationProviderService so it can be ready to
        // get locations ASAP.
        Intent intent = new Intent(this, LocationProviderService.class);
        this.startService(intent);
    }

    /**
     * This is called when the loader is first made. It is necessary to implement the LoaderManager
     * callbacks.
     *
     * @param id the id of the loader, which is not needed because there is only one
     * @param args miscellaneous arguments to help make the loader, of which there are none
     * @return returns a new CursorLoader
     */
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // The String[] argument contains the a projection of the relevant columns.
        String[] cols = new String[] {ContactEntry.COLUMN_NAME, ContactEntry.COLUMN_AUTOSHARE};
        return new CursorLoader(this, null, cols, null, null, null) {
            // loadInBackground executes the query (which selects all contacts) in the background,
            // off the UI thread.
            @Override
            public Cursor loadInBackground() {
                return new QueryCommand(MainActivity.this, ContactEntry.TABLE_NAME, false,
                        new String[] {ContactEntry.COLUMN_NAME, ContactEntry.COLUMN_AUTOSHARE,
                                ContactEntry._ID, ContactEntry.COLUMN_COLOR},
                        null, null, null, null, null, null).execute();
            }
        };
    }

    /**
     * Swap the new cursor in. LoaderManager will take care of closing the old cursor. This is
     * needed to implement LoaderManager callbacks.
     *
     * @param loader the loader to apply the callbacks to
     * @param data the data to go into the loader
     */
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    // This is called when the last Cursor provided to onLoadFinished() is about to be closed.
    // Swapping with null ensures the cursor is no longer being used.
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    protected void onPause() {
        mMenu.close(false);
        super.onPause();
    }

    /**
     * Handles permission requests for location and contacts.
     *
     * @param request_code the code for the permission being requested
     * @param permissions a String of the permissions being requested
     * @param grant_results whether or not the permission was granted
     */
    @Override
    public void onRequestPermissionsResult(int request_code, @NonNull String permissions[],
                                           @NonNull int[] grant_results) {
        switch (request_code) {
            case Constants.WHEREUAT_PERMISSION_REQUEST_LOCATION: {
                if (grant_results.length > 0 &&
                        grant_results[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission granted!");
                } else {
                    Log.d(TAG, "permission not granted :(");
                }
                break;
            }
            case Constants.WHEREUAT_PERMISSION_REQUEST_READ_CONTACTS: {
                if (grant_results[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    showContactsPhonebook();
                } else {
                    Toast.makeText(this,
                                   "Until you grant the permission, we cannot display the names",
                                   Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int first_visible_item, int visible_item_count,
                         int total_item_count) {
        mMenu.close(true);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scroll_state) { }

    /**
     * This class is for receiving broadcasts from the service that handles incoming GCM
     * notifications. It is responsible for sending @responses.
     */
    public static class AtResponseInitiateReceiver extends BroadcastReceiver {
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

    /**
     * Displays the contacts application if the contacts permission has been granted.
     *
     * @param view unused, only here so the function can be bound in the XML file
     */
    public void addContactOnClick(View view){
        mMenu.close(true);
        if (hasPermission(Manifest.permission.READ_CONTACTS)) {
            showContactsPhonebook();
        } else {
            requestPermission(Manifest.permission.READ_CONTACTS,
                              Constants.WHEREUAT_PERMISSION_REQUEST_READ_CONTACTS);
        }
    }

    private boolean hasPermission(String permission) {
        int perm_state = ContextCompat.checkSelfPermission(this, permission);
        return perm_state == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission, int permission_request) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, permission_request);
    }

    private void showContactsPhonebook() {
        // An intent must be sent to the contacts application so it can be started from this app.
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    public void showKeyLocDialog(View view) {
        mMenu.close(true);

        FragmentManager fm = getFragmentManager();
        KeyLocDialogFragment key_loc_dialog = new KeyLocDialogFragment();
        key_loc_dialog.show(fm, TAG);
    }

    /**
     * Used to catch the contact returned from the contacts application after a contact was
     * selected, this function will try to insert the contact into the database if it is not there.
     *
     * @param req_code the request code that triggered this function call
     * @param result_code the result code returned by the child
     * @param data an intent used to give data back to the caller
     */
    @Override
    public void onActivityResult(int req_code, int result_code, Intent data) {
        super.onActivityResult(req_code, result_code, data);
        ContactRetriever con = new ContactRetriever(req_code, result_code, data, this);

        final String name = con.getContactName();
        final String phone = con.getPhoneNumber();
        final Context context = this;
        if (name != null && phone != null) {
            String[] cols = {};
            QueryCommand query = ContactUtils.buildSelectContactByPhoneCommand(this, phone, cols);
            // Execute the command that checks if the contact is already in the database.
            new DbTask() {
                @Override
                public void onPostExecute(Object result) {
                    Cursor c = (Cursor) result;
                    boolean exists = c.getCount() > 0;
                    // Make sure the contact does not exist before inserting it into the database.
                    if (!exists) {
                        InsertCommand insert = ContactUtils.buildInsertCommand(context, name, phone,
                                false, generateRandomColor());
                        // Execute the command to insert the contact into the database.
                        new DbTask() {
                            @Override
                            public void onPostExecute(Object result) {
                                if ((Long) result != -1) {
                                    Log.d(TAG, "Successfully inserted");

                                    String[] select_cols = {ContactEntry.COLUMN_NAME,
                                            ContactEntry.COLUMN_AUTOSHARE,
                                            ContactEntry._ID,
                                            ContactEntry.COLUMN_COLOR};
                                    QueryCommand query = ContactUtils.buildSelectAllCommand(context,
                                            select_cols);
                                    new DbTask() {
                                        @Override
                                        public void onPostExecute(Object result) {
                                            try {
                                                // Refresh the data in the GridView
                                                mAdapter.swapCursor((Cursor) result);
                                            } catch (NullPointerException e) {
                                                Log.d(TAG, "Null contact cursor");
                                            }
                                        }
                                    }.execute(query);
                                } else {
                                    Log.d(TAG, "Some weird things happened when inserting into DB");
                                }
                            }
                        }.execute(insert);
                    } else {
                        String msg = "That phone number's already added!";
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    }
                }
            }.execute(query);
        } else {
            Log.d(TAG, String.format("name: %s, phone: %s", name, phone));
            Toast.makeText(this, "Couldn't add that contact", Toast.LENGTH_SHORT).show();
        }
    }

    private int generateRandomColor() {
        Random rnd = new Random();
        // A number between 0 and 360.
        float hue = rnd.nextInt(360);
        // The multiplier keeps the value in a range, the addition keeps the number farther from 0
        // so colors that are almost black aren't generated.
        float value = rnd.nextFloat() * 0.4f + 0.4f;
        // Keep the saturation constant at 0.3.
        return Color.HSVToColor(new float[] {hue, 0.3f, value});
    }
}



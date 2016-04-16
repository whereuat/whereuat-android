package xyz.whereuat.whereuat;

import android.Manifest;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import xyz.whereuat.whereuat.db.DbTask;
import xyz.whereuat.whereuat.db.command.InsertCommand;
import xyz.whereuat.whereuat.db.command.QueryCommand;
import xyz.whereuat.whereuat.db.entry.ContactEntry;

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

        GridView gridview = (GridView)findViewById(R.id.contact_gridview);
        gridview.setAdapter(mAdapter);
        gridview.setOnScrollListener(this);
        getSupportLoaderManager().initLoader(0, null, this);

        String location_permission = Manifest.permission.ACCESS_FINE_LOCATION;

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

    //
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // The String[] argument contains the a projection of the relevant columns.
        return new CursorLoader(this, null,
                new String[] {ContactEntry.COLUMN_NAME, ContactEntry.COLUMN_AUTOSHARE}, null, null,
                null) {
            // loadInBackground executes the query (which selects all contacts) in the background,
            // off the UI thread.
            @Override
            public Cursor loadInBackground() {
                return new QueryCommand(MainActivity.this, ContactEntry.TABLE_NAME, false,
                        new String[] {ContactEntry.COLUMN_NAME, ContactEntry.COLUMN_AUTOSHARE,
                                ContactEntry._ID},
                        null, null, null, null, null, null).execute();
            }
        };
    }

    // Swap the new cursor in. LoaderManager will take care of closing the old cursor.
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

    public static class AtResponseInitiateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            NotificationManager nm = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            nm.cancel(intent.getIntExtra(Constants.NOTIFICATION_ID_EXTRA, 0));

            Location loc = LocationProviderService.getLocation();
            try {
                (new HttpRequestHandler(context)).postAtResponse(
                        (new PreferenceController(context)).getClientPhoneNumber(),
                        intent.getStringExtra(Constants.TO_PHONE_EXTRA), loc.getLatitude(),
                        loc.getLongitude(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                String text = "Got a 200 from the @response POST!";
                                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
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
    }

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
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    public void showKeyLocDialog(View view) {
        mMenu.close(true);

        FragmentManager fm = getFragmentManager();
        KeyLocDialogFragment key_loc_dialog = new KeyLocDialogFragment();
        key_loc_dialog.show(fm, TAG);
    }

    @Override
    public void onActivityResult(int req_code, int result_code, Intent data) {
        super.onActivityResult(req_code, result_code, data);
        ContactRetriever con = new ContactRetriever(req_code, result_code, data, this);

        String name = con.getContactName();
        String phone = con.getPhoneNumber();
        if (name != null && phone != null) {
            InsertCommand insert = ContactUtils.buildInsertCommand(this, name, phone, false);
            new DbTask() {
                @Override
                public void onPostExecute(Object result) {
                    if ((Long) result != -1) {
                        Log.d(TAG, "Successfully inserted");
                    } else {
                        Log.d(TAG, "Some weird things happened when inserting into DB");
                    }
                }
            }.execute(insert);

            String[] selectCols = {ContactEntry.COLUMN_NAME, ContactEntry.COLUMN_AUTOSHARE,
                                   ContactEntry._ID};
            QueryCommand query = ContactUtils.buildSelectAllCommand(this, selectCols);
            new DbTask() {
                @Override
                public void onPostExecute(Object result) {
                    Cursor c = (Cursor) result;
                    try {
                        // Refresh the data in the GridView
                        mAdapter.swapCursor(c);
                        mAdapter.notifyDataSetChanged();
                    } catch (NullPointerException e) {
                        Log.d(TAG, "Null contact cursor");
                    }
                }
            }.execute(query);
        } else {
            Log.d(TAG, String.format("name: %s, phone: %s", name, phone));
            Toast.makeText(this, "Couldn't add that contact", Toast.LENGTH_SHORT).show();
        }
    }
}



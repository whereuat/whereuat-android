package xyz.whereuat.whereuat;

import android.Manifest;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;

import xyz.whereuat.whereuat.db.entry.ContactEntry;
import xyz.whereuat.whereuat.ui.adapters.ContactCardCursorAdapter;
import xyz.whereuat.whereuat.ui.views.KeyLocDialogFragment;
import xyz.whereuat.whereuat.utils.ContactUtils;
import xyz.whereuat.whereuat.utils.LocationProviderService;
import xyz.whereuat.whereuat.utils.PhonebookUtils;

/**
 * This class contains the main contact grid view full of ContactCard squares. It is responsible
 * for loading a cursor with data from the contacts table and for providing buttons for adding key
 * locations and contacts.
 */
public class MainActivity extends DrawerActivity implements OnScrollListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private FloatingActionMenu mMenu;
    private ContactCardCursorAdapter mAdapter;
    private ReloadContactsReceiver mReloadContactsReceiver;
    private IntentFilter mReloadContactsFilter;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMenu = (FloatingActionMenu) findViewById(R.id.menu);
        initDrawer(getString(R.string.app_name));
        initContactGrid();
        initPermissionRequests();

        // When the activity is created start the LocationProviderService so it can be ready to
        // get locations ASAP.
        Intent intent = new Intent(this, LocationProviderService.class);
        this.startService(intent);

        mReloadContactsReceiver = new ReloadContactsReceiver();
        mReloadContactsFilter = new IntentFilter(Constants.RELOAD_CONTACTS_BROADCAST);
    }

    /**
     * Initializes the Contacts adapter and GridView.
     */
    private void initContactGrid() {
        mAdapter = new ContactCardCursorAdapter(this);
        // Connect the contact GridView into the adapter that holds the data from the database.
        GridView gridview = (GridView)findViewById(R.id.contact_gridview);
        gridview.setAdapter(mAdapter);
        gridview.setOnScrollListener(this);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    /**
     * Handles initially asking for permissions if they have not been granted yet.
     */
    private void initPermissionRequests() {
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
                return (Cursor) ContactUtils.buildSelectAllCommand(MainActivity.this).call();
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
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mReloadContactsReceiver,
                mReloadContactsFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReloadContactsReceiver);
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
                if (grant_results.length > 0 &&
                        grant_results[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    showContactsPhonebook();
                } else {
                    Toast.makeText(this,
                                   "Couldn't open phone book. Permission not granted.",
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
        intent.setType(Phone.CONTENT_TYPE);
        startActivityForResult(intent, Constants.PHONEBOOK_PICK_REQUEST);
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
     * @param intent an intent used to give data back to the caller
     */
    @Override
    public void onActivityResult(int req_code, int result_code, Intent intent) {
        super.onActivityResult(req_code, result_code, intent);

        switch(req_code) {
            case(Constants.PHONEBOOK_PICK_REQUEST):
                if (result_code == AppCompatActivity.RESULT_OK) {
                    onPhonebookPickResult(intent);
                }
                break;
        }
    }

    /**
     * Process the result of the phone book activity
     *
     * @param intent Intent object carrying the data about the phone book selection
     */
    private void onPhonebookPickResult(Intent intent) {
        // Retrieve the contact
        ContactUtils.Contact contact = PhonebookUtils.getContactFromPhonebook(intent, this);

        final String name = contact.getName();
        final String phone = contact.getPhone();
        final Context context = this;
        if (name != null && phone != null) {
            AsyncExecutor.service.submit(new Runnable() {
                @Override
                public void run() {
                    String[] cols = {};
                    Cursor contact = ContactUtils.buildSelectContactByPhoneCommand(context,
                            phone, cols).call();
                    boolean exists = contact.getCount() > 0;
                    if (!exists) {
                        Long result = ContactUtils.buildInsertCommand(context, name, phone,
                                false, ContactUtils.generateRandomColor()).call();
                        if (result != -1) {
                            Log.d(TAG, "Successfully inserted");

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Cursor all_contacts = ContactUtils.buildSelectAllCommand(
                                            context).call();
                                    try {
                                        mAdapter.swapCursor(all_contacts);
                                    } catch (NullPointerException e) {
                                        Log.d(TAG, "Null contact cursor");
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "Some weird things happened when inserting into DB");
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String msg = "That phone number's already added!";
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        } else {
            Log.d(TAG, String.format("name: %s, phone: %s", name, phone));
            Toast.makeText(this, "Couldn't add that contact", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This function is so other classes can notify this activity that the contacts have changed
     * and should be reloaded.
     *
     * @param context the context that is notifying the change
     */
    public static void notifyOfContactChange(Context context) {
        Intent intent = new Intent(Constants.RELOAD_CONTACTS_BROADCAST);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * This receiver reloads the GridView of contacts whenever it receives a new broadcast. This
     * class is necessary so other parts of the application can trigger a cursor reload so the UI
     * updates.
     */
    class ReloadContactsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.swapCursor(ContactUtils.buildSelectAllCommand(context).call());
        }
    }
}



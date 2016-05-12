package xyz.whereuat.whereuat;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.Random;

import xyz.whereuat.whereuat.db.entry.ContactEntry;
import xyz.whereuat.whereuat.ui.adapters.ContactCardCursorAdapter;
import xyz.whereuat.whereuat.ui.adapters.DrawerListAdapter;
import xyz.whereuat.whereuat.ui.views.KeyLocDialogFragment;
import xyz.whereuat.whereuat.ui.views.LatoTextView;
import xyz.whereuat.whereuat.utils.ContactUtils;
import xyz.whereuat.whereuat.utils.DrawerItem;
import xyz.whereuat.whereuat.utils.LocationProviderService;
import xyz.whereuat.whereuat.utils.PhonebookUtils;
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
    private Toolbar mToolbar;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mMenu = (FloatingActionMenu) findViewById(R.id.menu);
        initContactGrid();
        initDrawer();
        initPermissionRequests();

        // When the activity is created start the LocationProviderService so it can be ready to
        // get locations ASAP.
        Intent intent = new Intent(this, LocationProviderService.class);
        this.startService(intent);
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
     * Creates the slide-out drawer and populates it with list items.
     */
    private void initDrawer() {
        ((LatoTextView) findViewById(R.id.drawer_phone)).setText(
                new PreferenceController(this).getClientPhoneNumber());

        DrawerLayout drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        String[] menu_titles = getResources().getStringArray(R.array.drawer_items);
        TypedArray menu_icons = getResources().obtainTypedArray(R.array.drawer_icons);

        // Fill the list with all of the items to be displayed.
        ArrayList<DrawerItem> drawer_items = new ArrayList<>();
        for (int i = 0; i < menu_icons.length(); ++i) {
            drawer_items.add(new DrawerItem(menu_titles[i], menu_icons.getResourceId(i, -1)));
        }

        ((ListView) findViewById(R.id.drawer_list)).setAdapter(
                new DrawerListAdapter(this, drawer_items));

        ActionBarDrawerToggle drawer_toggle = new ActionBarDrawerToggle(this, drawer_layout,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.setDrawerListener(drawer_toggle);

        drawer_toggle.syncState();
        menu_icons.recycle();
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
                return (Cursor) ContactUtils.buildSelectAllCommand(MainActivity.this,
                        new String[] {ContactEntry.COLUMN_NAME, ContactEntry.COLUMN_AUTOSHARE,
                                ContactEntry._ID, ContactEntry.COLUMN_COLOR}).call();
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
                                false, generateRandomColor()).call();
                        if (result != -1) {
                            Log.d(TAG, "Successfully inserted");

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String[] select_cols = {ContactEntry.COLUMN_NAME,
                                            ContactEntry.COLUMN_AUTOSHARE,
                                            ContactEntry._ID,
                                            ContactEntry.COLUMN_COLOR};
                                    Cursor all_contacts = ContactUtils.buildSelectAllCommand(
                                            context, select_cols).call();
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



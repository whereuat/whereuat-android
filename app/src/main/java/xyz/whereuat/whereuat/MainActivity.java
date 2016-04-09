package xyz.whereuat.whereuat;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;

import xyz.whereuat.whereuat.db.DbTask;
import xyz.whereuat.whereuat.db.command.InsertCommand;
import xyz.whereuat.whereuat.db.entry.ContactEntry;

public class MainActivity extends AppCompatActivity implements OnScrollListener {
    private FloatingActionMenu mMenu;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMenu = (FloatingActionMenu) findViewById(R.id.menu);

        GridView gridview = (GridView)findViewById(R.id.contact_gridview);
        gridview.setAdapter(new SquareAdapter(this));
        gridview.setOnScrollListener(this);

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

        Intent intent = new Intent(this, LocationProviderService.class);
        intent.putExtra(Constants.SHOULD_START_LOCATION_SERVICE, true);
        this.startService(intent);
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
                    Intent intent= new Intent(Intent.ACTION_PICK,
                                              ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, 1);
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
        public void onReceive(Context context, Intent intent) {
            NotificationManager nm = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            nm.cancel(intent.getIntExtra(Constants.NOTIFICATION_ID_EXTRA, 0));

            Intent loc_intent = new Intent(context, LocationProviderService.class);
            String to_phone = intent.getStringExtra(Constants.TO_PHONE_EXTRA);
            loc_intent.putExtra(Constants.TO_PHONE_EXTRA, to_phone);
            context.startService(loc_intent);
        }
    }

    public static class AtResponseLocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            double lat = intent.getDoubleExtra(Constants.CURR_LATITUDE_EXTRA, -1.0);
            double lng = intent.getDoubleExtra(Constants.CURR_LONGITUDE_EXTRA, -1.0);
            (new HttpRequestHandler(context)).postAtResponse(
                    (new PreferenceController(context)).getClientPhoneNumber(),
                    intent.getStringExtra(Constants.TO_PHONE_EXTRA), lat, lng);
//            context.startService(new Intent(context, LocationProvider.class));
        }
    }

    /*
        Receives the response code from the @response POST
     */
    public static class AtResponseReceiver extends BroadcastReceiver {
        String text;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra(Constants.RESPONSE_CODE_EXTRA, 400) == 200) {
                text = "Got a 200 from the @response POST!";
            } else {
                text = "Error sending the location POST " +
                        Integer.toString(intent.getIntExtra(Constants.RESPONSE_CODE_EXTRA, 400));
            }
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    public void addContact(View view){
        mMenu.close(true);
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int contacts_permission = checkSelfPermission(Manifest.permission.READ_CONTACTS);
            if (contacts_permission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                                   Constants.WHEREUAT_PERMISSION_REQUEST_READ_CONTACTS);
            } else {
                Uri content_uri = ContactsContract.Contacts.CONTENT_URI;
                Intent intent = new Intent(Intent.ACTION_PICK, content_uri);
                startActivityForResult(intent, 1);
            }
        }
    }

    public void addKeyLoc(View view) {
        mMenu.close(true);
    }

    @Override
    public void onActivityResult(int req_code, int result_code, Intent data) {
        super.onActivityResult(req_code, result_code, data);
        ContactRetriever con = new ContactRetriever(req_code, result_code, data, this);

        String name = con.getContactName();
        String phone = con.getPhoneNumber();
        if (name != null && phone != null){
            ContentValues values = new ContentValues();
            values.put(ContactEntry.COLUMN_NAME, con.getContactName());
            values.put(ContactEntry.COLUMN_PHONE, con.getPhoneNumber());
            values.put(ContactEntry.COLUMN_AUTOSHARE, false);

            InsertCommand cmd = new InsertCommand(this, ContactEntry.TABLE_NAME, null, values);
            new DbTask(new DbTask.AsyncResponse() {
                @Override
                public void processFinish(Object result) {
                    if ((Long) result != -1) {
                        Log.d(TAG, "Successfully inserted contact");
                    } else {
                        Log.d(TAG, "Some weird things happened when inserting contact into DB");
                    }
                }
            }).execute(cmd);
        } else {
            Log.d(TAG, String.format("name: %s, phone: %s", name, phone));
            Toast.makeText(this, "Couldn't add that contact", Toast.LENGTH_SHORT).show();
        }
    }
}



package com.whereuat.whereu;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;
import android.os.Build;
import android.provider.ContactsContract;
import android.view.View;


public class MainActivity extends AppCompatActivity {
    private IntentFilter mLocationFilter;
    private LocationReceiver mLocationReceiver;
    private final String TAG = "MainActivity";

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridview = (GridView)findViewById(R.id.contact_gridview);
        gridview.setAdapter(new SquareAdapter(this));

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

        mLocationFilter = new IntentFilter(Constants.LOCATION_BROADCAST);
        mLocationReceiver = new LocationReceiver();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mLocationReceiver, mLocationFilter);
    }

    @Override
    protected void onPause() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationReceiver);
        } catch (IllegalArgumentException e) {}
        super.onPause();
    }

    @Override
    public void onResume() {
        try {
            LocalBroadcastManager.getInstance(this)
                    .registerReceiver(mLocationReceiver, mLocationFilter);
        } catch (IllegalArgumentException e) {}
        super.onResume();
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
            }
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grant_results[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    Intent intent= new Intent(Intent.ACTION_PICK,  ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, 1);
                } else {
                    Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class LocationReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            double lat = intent.getDoubleExtra(Constants.CURR_LATITUDE_EXTRA, -1.0);
            double lon = intent.getDoubleExtra(Constants.CURR_LONGITUDE_EXTRA, -1.0);
            String text = String.format("Lat: %.5f, Lon: %.5f", lat, lon);
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    public void addContact(View view){
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            Intent intent= new Intent( Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, 1);
        }
    }

    // TODO: get contact name and phone number from the ContractRetriever object.
    // Use this information to add a new contact to the database.
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        ContactRetriever con = new ContactRetriever(reqCode,resultCode, data, this.getApplicationContext());
    }
}



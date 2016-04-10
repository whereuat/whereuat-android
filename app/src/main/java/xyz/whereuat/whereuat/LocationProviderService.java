package xyz.whereuat.whereuat;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

/**
 * Created by julius on 4/4/16.
 *
 * This Service gives the device's current location.
 */
public class LocationProviderService extends Service implements OnConnectionFailedListener,
        ConnectionCallbacks {
    private static GoogleApiClient mGoogleApiClient;
    private static final String TAG = "LocationServiceProvider";

    // This function is here just so the service can start.
    @Override
    public int onStartCommand(Intent intent, int flags, int start_id) {
        return START_STICKY;
    }

    public static Location getLocation() {
        try {
            return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            Log.d(TAG, "No location permission");
        } catch (NullPointerException e) {
            Log.d(TAG, "Null API client");
        }
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {}

    @Override
    public void onConnected(Bundle connection_hint) {}

    @Override
    public void onConnectionSuspended(int cause) {}
}

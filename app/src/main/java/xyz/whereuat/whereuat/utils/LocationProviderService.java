package xyz.whereuat.whereuat.utils;

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
 * Gets the device's current location.
 */
public class LocationProviderService extends Service implements OnConnectionFailedListener,
        ConnectionCallbacks {
    private static GoogleApiClient mGoogleApiClient;
    private static final String TAG = "LocationServiceProvider";

    /**
     * Start the service
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int start_id) {
        return START_STICKY;
    }

    /**
     * Get the client's current location (or best possible guess)
     *
     * @return Location object containing the client's current location, null if the location could
     *         not be found for whatever reason
     */
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

    /**
     * Override this method so the service can bind
     */
    @Override
    public IBinder onBind(Intent intent) { return null; }

    /**
     * Connect to the Google API when the service is created
     */
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

    /**
     * Disconnect from the Google API when the service is destroyed
     */
    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
    }

    /**
     * Necessary Override for OnConnectionFailedListener
     *
     * @param result Result of failed connection
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {}

    /**
     * Necessary Override for ConnectionCallbacks
     *
     * @param connection_hint Hint for the connection
     */
    @Override
    public void onConnected(Bundle connection_hint) {}

    /**
     * Necessary Override for ConnectionCallbacks
     *
     * @param cause Cause for suspended connection
     */
    @Override
    public void onConnectionSuspended(int cause) {}
}

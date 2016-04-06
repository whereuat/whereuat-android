package xyz.whereuat.whereuat;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

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
    private GoogleApiClient mGoogleApiClient;
    private final String TAG = "LocationServiceProvider";

    @Override
    public int onStartCommand(Intent intent, int flags, int start_id) {
        Location location;
        // Only get locations if the service doesn't need to be started.
        if (!intent.getBooleanExtra(Constants.SHOULD_START_LOCATION_SERVICE, false)) {
            try {
                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                double lon = location.getLongitude();
                double lat = location.getLatitude();
                sendLocationRequestBroadcast(lon, lat);
            } catch (SecurityException e) {
                String text = "You need give location permissions to use whereu@.";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                Log.d(TAG, "null loc");
                // the location was probably null.
            }
        }
        return START_STICKY;
    }

    private void sendLocationRequestBroadcast(double lon, double lat) {
        Intent result = new Intent();
        result.putExtra(Constants.CURR_LONGITUDE_EXTRA, lon);
        result.putExtra(Constants.CURR_LATITUDE_EXTRA, lat);
        result.setAction(Constants.LOCATION_BROADCAST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(result);
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

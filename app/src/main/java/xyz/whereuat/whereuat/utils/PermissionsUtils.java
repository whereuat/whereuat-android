package xyz.whereuat.whereuat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by kangp3 on 6/6/16.
 */
public class PermissionsUtils {
    private static final String TAG = "PermUtils";

    public static boolean hasPermission(Context context, String permission) {
        int perm_state = ContextCompat.checkSelfPermission(context, permission);
        return perm_state == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity, String permission, int permission_req) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, permission_req);
        }
    }
}

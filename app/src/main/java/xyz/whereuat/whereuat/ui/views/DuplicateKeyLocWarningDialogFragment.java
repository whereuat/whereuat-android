package xyz.whereuat.whereuat.ui.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import xyz.whereuat.whereuat.AsyncExecutor;
import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.utils.KeyLocationUtils;

/**
 * This class is used for handling the dialog that is displayed when the user attempts to add a key
 * location with a name that already exists in the database
 */
public class DuplicateKeyLocWarningDialogFragment extends DialogFragment {
    private static final String TAG = "KeyLocWarnFragment";

    public static DuplicateKeyLocWarningDialogFragment newInstance(String key_loc_name,
                                                                   Location loc) {
        DuplicateKeyLocWarningDialogFragment frag = new DuplicateKeyLocWarningDialogFragment();
        Bundle args = new Bundle();
        args.putString("name", key_loc_name);
        args.putDouble("lat", loc.getLatitude());
        args.putDouble("lng", loc.getLongitude());
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        final String name = getArguments().getString("name");
        final Double latitude = getArguments().getDouble("lat");
        final Double longitude = getArguments().getDouble("lng");

        return new AlertDialog.Builder(activity, R.style.DialogTheme)
                .setTitle(activity.getString(R.string.duplicate_location_prompt_title_text))
                .setMessage(String.format(activity.getString(R.string.duplicate_location_body_text),
                        name))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AsyncExecutor.service.submit(new Runnable() {
                            @Override
                            public void run() {
                                Long result= KeyLocationUtils.buildInsertCommand(activity, name,
                                        latitude, longitude).call();
                                if (result != -1) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(activity, activity.getResources()
                                                    .getText(R.string.key_loc_insert_success),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Log.d(TAG, "Some weird things happened when inserting into DB");
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }
}

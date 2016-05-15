package xyz.whereuat.whereuat.ui.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import xyz.whereuat.whereuat.AsyncExecutor;
import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.utils.KeyLocationUtils;
import xyz.whereuat.whereuat.utils.LocationProviderService;

/**
 * Created by kangp3 on 4/8/16.
 *
 * This class is used for handling the dialog that is displayed when the user clicks to add a key
 * location to the database.
 */
public class KeyLocDialogFragment extends DialogFragment {
    public static final String TAG = "KeyLocFragment";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.DialogTheme);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.key_loc_dialog, (ViewGroup) this.getView());
        builder.setView(view)
               .setTitle(R.string.key_location_prompt_title_text)
                // When clicked, the "Okay" button should add the key location to the database if it
                // is valid.
               .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       Dialog d = (Dialog) dialog;
                       EditText e = (EditText) d.findViewById(R.id.key_loc_name_input);

                       final String name = e.getText().toString();
                       final Location loc = LocationProviderService.getLocation();

                       // Make sure the name is not null or empty and the location is not null.
                       boolean name_is_valid = KeyLocationUtils.nameIsValid(name),
                               loc_is_valid = KeyLocationUtils.locIsValid(loc);
                       if (name_is_valid && loc_is_valid) {
                           AsyncExecutor.service.submit(new Runnable() {
                               @Override
                               public void run() {
                                   Cursor loc_cursor = KeyLocationUtils.buildSelectNameCommand(
                                           activity, new String[] {}, name).call();
                                   if (loc_cursor.getCount() == 0) {
                                       Long result = KeyLocationUtils.buildInsertCommand(activity,
                                               name, loc.getLatitude(), loc.getLongitude()).call();
                                       if (result != -1) {
                                           activity.runOnUiThread(new Runnable() {
                                               @Override
                                               public void run() {
                                                   Toast.makeText(activity, activity.getResources()
                                                           .getText(R.string
                                                                   .key_loc_insert_success),
                                                           Toast.LENGTH_SHORT).show();
                                               }
                                           });
                                       } else {
                                           Log.d(TAG, "Bad things happened when inserting into DB");
                                       }
                                   } else {
                                       FragmentManager fm = getFragmentManager();
                                       DuplicateKeyLocWarningDialogFragment.newInstance(name, loc)
                                               .show(fm, TAG);
                                   }
                               }
                           });
                       } else if (!name_is_valid) {
                           String err_str = "Invalid location name";
                           Toast.makeText(activity, err_str, Toast.LENGTH_SHORT).show();
                       } else if (!loc_is_valid) {
                           String err_str = "Location not found";
                           Toast.makeText(activity, err_str, Toast.LENGTH_SHORT).show();
                       }
                   }
               })
               // The "Cancel" button shouldn't do anything.
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {

                   }
               });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog d = (AlertDialog) getDialog();

        EditText e = (EditText) d.findViewById(R.id.key_loc_name_input);
        // Automatically show the keyboard when the dialog appears.
        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    d.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
    }
}

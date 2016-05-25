package xyz.whereuat.whereuat.ui.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import xyz.whereuat.whereuat.AsyncExecutor;
import xyz.whereuat.whereuat.Constants;
import xyz.whereuat.whereuat.KeyLocationActivity;
import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.utils.KeyLocationUtils;

/**
 * This class is used for handling the dialog that is displayed when the user clicks to edit a key
 * location.
 */
public class EditKeyLocationFragment extends DialogFragment {
    public static final String TAG = "EditKeyLocationFragment";

    public static EditKeyLocationFragment newInstance(int id) {
        EditKeyLocationFragment frag = new EditKeyLocationFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.KEY_LOC_ID_BUNDLE_EXTRA, id);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.DialogTheme);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.key_loc_dialog, (ViewGroup) this.getView());
        builder.setView(view)
                .setTitle("Edit Key Location")
                // When clicked, the "Okay" button should edit the key location's name in the
                // database if it is valid.
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog d = (Dialog) dialog;
                        EditText e = (EditText) d.findViewById(R.id.key_loc_name_input);

                        final String new_name = e.getText().toString();

                        // Make sure the name is not null or empty.
                        boolean name_is_valid = KeyLocationUtils.nameIsValid(new_name);
                        if (name_is_valid) {
                            updateKeyLocName(activity, getArguments().getInt(
                                    Constants.KEY_LOC_ID_BUNDLE_EXTRA), new_name);
                        } else {
                            Toast.makeText(activity, "Invalid location name.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                // The "Cancel" button shouldn't do anything.
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
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

    /**
     * Tries to edit the key location's name and displays a toast about if the update was successful
     * or not.
     *
     * @param activity the activity that created the dialog
     * @param id the id of the key location to update
     * @param new_name the new name for the key location
     */
    private void updateKeyLocName(final Activity activity, final int id, final String new_name) {
        AsyncExecutor.service.submit(new Runnable() {
            @Override
            public void run() {
                final int updated = KeyLocationUtils.buildUpdateNameByIdCommand(activity, id,
                        new_name).call();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (updated == 1) {
                            KeyLocationActivity.notifyOfKeyLocationChange(activity);
                            Toast.makeText(activity, "Key location updated!", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(activity, "Key location couldn't be updated :(",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}

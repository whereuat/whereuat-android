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
import xyz.whereuat.whereuat.ContactRequestsActivity;
import xyz.whereuat.whereuat.MainActivity;
import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.utils.ContactUtils;
import xyz.whereuat.whereuat.utils.ContactRequestUtils;

/**
 * This class is used for handling the dialog that is displayed when the user tries to add a pending
 * contact request for a number that was not found in the phonebook.
 */
public class AddContactDialogFragment extends DialogFragment {
    public static final String TAG = "AddContactDialogFrag";

    @Override
    public Dialog onCreateDialog(final Bundle saved_instance_state) {
        final Activity activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.DialogTheme);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_contact_dialog, (ViewGroup) this.getView());
        builder.setView(view)
                .setTitle(R.string.add_contact_prompt_title_text)
                // When clicked, the "Okay" button should add the contact to the database if the
                // name is valid.
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dialog d = (Dialog) dialog;
                        EditText e = (EditText) d.findViewById(R.id.contact_name_input);

                        String name = e.getText().toString();

                        // Make sure the name is not null or empty.
                        if (!name.isEmpty()) {
                            addContactFromPending(activity, name, getArguments().getString(
                                    Constants.CONTACT_NUMBER_BUNDLE_EXTRA));
                        } else {
                            String err_str = "Invalid contact name";
                            Toast.makeText(activity, err_str, Toast.LENGTH_SHORT).show();
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

    /**
     * This function wraps an insert into the contacts table and deletes them from the pending
     * contacts.
     *
     * @param activity the Activity that the fragment comes from
     * @param name the name of the new contact
     * @param number the number of the new contact
     */
    private void addContactFromPending(final Activity activity, final String name,
                                       final String number) {
        AsyncExecutor.service.submit(new Runnable() {
            @Override
            public void run() {
                final int num_deleted = ContactRequestUtils.buildDeleteByPhoneCommand(activity,
                        number).call();
                final long inserted = ContactUtils.buildInsertCommand(activity, name, number,
                        false, ContactUtils.generateRandomColor(), 0).call();
                // Show success and failure Toasts.
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (num_deleted == 1 && inserted != -1) {
                            MainActivity.notifyOfContactChange(activity);
                            ContactRequestsActivity.notifyOfContactRequestChange(activity);
                            Toast.makeText(activity, "New contact added.", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(activity, "Error adding the new contact.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        final AlertDialog d = (AlertDialog) getDialog();

        EditText e = (EditText) d.findViewById(R.id.contact_name_input);
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

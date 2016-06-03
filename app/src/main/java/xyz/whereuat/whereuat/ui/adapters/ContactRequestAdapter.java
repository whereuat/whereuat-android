package xyz.whereuat.whereuat.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.Toast;

import xyz.whereuat.whereuat.AsyncExecutor;
import xyz.whereuat.whereuat.Constants;
import xyz.whereuat.whereuat.ContactRequestsActivity;
import xyz.whereuat.whereuat.MainActivity;
import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.db.entry.ContactRequestEntry;
import xyz.whereuat.whereuat.ui.views.AddContactDialogFragment;
import xyz.whereuat.whereuat.ui.views.LatoTextView;
import xyz.whereuat.whereuat.utils.ContactUtils;
import xyz.whereuat.whereuat.utils.ContactRequestUtils;

/**
 * This class is an adapter for creating list items in the Pending Requests page.
 */
public class ContactRequestAdapter extends SimpleCursorAdapter {
    private static String TAG = "ContactRequestAdapter";
    // Each item in |bound_cols| should correspond to the item in |bound_views| where its data is
    // used.
    private static final String[] BOUND_COLS = new String[] {ContactRequestEntry._ID};
    private static final int[] BOUND_VIEWS = new int[] {R.id.contact_req_item};
    private static ContactRequestsActivity mActivity;

    public ContactRequestAdapter(ContactRequestsActivity activity) {
        super(activity, R.layout.contact_req_list_item, null, BOUND_COLS, BOUND_VIEWS, 0);
        mActivity = activity;
    }

    @Override
    public void bindView (View view, final Context context, Cursor cursor) {
        final String name = cursor.getString(
                cursor.getColumnIndex(ContactRequestEntry.COLUMN_NAME));
        final String number = cursor.getString(
                cursor.getColumnIndex(ContactRequestEntry.COLUMN_PHONE));

        if (name.isEmpty()) {
            ((LatoTextView) view.findViewById(R.id.contact_req_contact_name)).setText(number);
            ((LatoTextView) view.findViewById(R.id.pending_req_contact_number)).setText("");
        } else {
            ((LatoTextView) view.findViewById(R.id.contact_req_contact_name)).setText(name);
            ((LatoTextView) view.findViewById(R.id.pending_req_contact_number)).setText(number);
        }

        // Set up the click listeners for the buttons.
        (view.findViewById(R.id.add_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.isEmpty()) {
                    handleWithAddContactDialog(number);
                } else {
                    addContact(context, name, number);
                }
            }
        });
        (view.findViewById(R.id.delete_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDelete(context, number);
            }
        });
    }

    /**
     * If the unknown contact has no name (meaning their number was not found in the phonebook),
     * a dialog for entering their name should be shown before the contact is added to the database.
     * This function displays the dialog and passes the contact's number to it. The dialog will
     * handle actually making the database insertion.
     *
     * @param number the new contact's phone number
     */
    private void handleWithAddContactDialog(String number) {
        FragmentManager fm = mActivity.getFragmentManager();
        AddContactDialogFragment add_contact_dialog = new AddContactDialogFragment();
        Bundle args = new Bundle();
        args.putString(Constants.CONTACT_NUMBER_BUNDLE_EXTRA, number);
        add_contact_dialog.setArguments(args);
        add_contact_dialog.show(fm, TAG);
    }

    /**
     * This function handles adding the contact to the database.
     *
     * @param context the context to execute the database insert from
     * @param name the new contact's name
     * @param number the new contact's number
     */
    private void addContact(final Context context, final String name, final String number) {
        AsyncExecutor.service.submit(new Runnable() {
            @Override
            public void run() {
                final int num_deleted = ContactRequestUtils.buildDeleteByPhoneCommand(context,
                        number).call();
                final long inserted = ContactUtils.buildInsertCommand(context, name, number,
                        false, ContactUtils.generateRandomColor(), 0).call();
                // Show success and failure Toasts.
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (num_deleted == 1 && inserted != -1) {
                            MainActivity.notifyOfContactChange(context);
                            ContactRequestsActivity.notifyOfContactRequestChange(context);
                            Toast.makeText(context, "New contact added.", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(context, "Error adding the new contact.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void handleDelete(final Context context, final String number) {
        AsyncExecutor.service.submit(new Runnable() {
            @Override
            public void run() {
                final int num_deleted = ContactRequestUtils.buildDeleteByPhoneCommand(context,
                        number).call();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (num_deleted == 1) {
                            ContactRequestsActivity.notifyOfContactRequestChange(context);
                            Toast.makeText(context, "Pending request deleted.", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(context, "Error deleting the pending request.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}

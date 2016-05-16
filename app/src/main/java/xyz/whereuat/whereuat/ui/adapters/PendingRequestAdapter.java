package xyz.whereuat.whereuat.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.Toast;

import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.db.entry.PendingRequestEntry;
import xyz.whereuat.whereuat.ui.views.LatoTextView;

/**
 * This class is an adapter for creating list items in the Pending Requests page.
 */
public class PendingRequestAdapter extends SimpleCursorAdapter {
    // Each item in |bound_cols| should correspond to the item in |bound_views| where its data is
    // used.
    private static final String[] BOUND_COLS = new String[] {PendingRequestEntry._ID};
    private static final int[] BOUND_VIEWS = new int[] {R.id.pending_req_item};

    public PendingRequestAdapter(Context context, MatrixCursor cursor) {
        super(context, R.layout.pending_req_list_item, cursor, BOUND_COLS, BOUND_VIEWS, 0);
    }

    @Override
    public void bindView (View view, Context context, Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(PendingRequestEntry.COLUMN_NAME));
        String number = cursor.getString(cursor.getColumnIndex(PendingRequestEntry.COLUMN_PHONE));

        if (name == null) {
            ((LatoTextView) view.findViewById(R.id.pending_req_contact_name)).setText(number);
        } else {
            ((LatoTextView) view.findViewById(R.id.pending_req_contact_name)).setText(name);
            ((LatoTextView) view.findViewById(R.id.pending_req_contact_number)).setText(number);
        }

        // Set up the click listeners for the buttons.
        (view.findViewById(R.id.add_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Adding contact...", Toast.LENGTH_SHORT).show();
            }
        });
        (view.findViewById(R.id.delete_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Deleting request...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

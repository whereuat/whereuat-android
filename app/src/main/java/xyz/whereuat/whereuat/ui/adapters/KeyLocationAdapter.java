package xyz.whereuat.whereuat.ui.adapters;

import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import xyz.whereuat.whereuat.AsyncExecutor;
import xyz.whereuat.whereuat.KeyLocationActivity;
import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.db.entry.KeyLocationEntry;
import xyz.whereuat.whereuat.ui.views.EditKeyLocationFragment;
import xyz.whereuat.whereuat.ui.views.LatoTextView;
import xyz.whereuat.whereuat.utils.KeyLocationUtils;

/**
 * This class is an adapter for creating list items in the Pending Requests page.
 */
public class KeyLocationAdapter extends SimpleCursorAdapter {
    private static final String TAG = "KeyLocationAdapter";
    // Each item in |bound_cols| should correspond to the item in |bound_views| where its data is
    // used.
    private static final String[] BOUND_COLS = new String[] {KeyLocationEntry._ID};
    private static final int[] BOUND_VIEWS = new int[] {R.id.key_loc_item};
    private static AppCompatActivity mActivity;

    public KeyLocationAdapter(AppCompatActivity activity) {
        super(activity, R.layout.key_loc_list_item, null, BOUND_COLS, BOUND_VIEWS, 0);
        mActivity = activity;
    }

    @Override
    public void bindView (View view, final Context context, final Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(KeyLocationEntry.COLUMN_NAME));
        float lat = cursor.getFloat(cursor.getColumnIndex(KeyLocationEntry.COLUMN_LATITUDE));
        float lng = cursor.getFloat(cursor.getColumnIndex(KeyLocationEntry.COLUMN_LONGITUDE));
        String coords = String.format("%f, %f", lat, lng);

        ((LatoTextView) view.findViewById(R.id.key_loc_name)).setText(name);
        ((LatoTextView) view.findViewById(R.id.key_loc_coords)).setText(coords);
        final int id = cursor.getInt(cursor.getColumnIndex(KeyLocationEntry._ID));

        // Set up the click listeners for the buttons.
        (view.findViewById(R.id.edit_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = mActivity.getFragmentManager();
                EditKeyLocationFragment.newInstance(id).show(fm, TAG);
            }
        });
        (view.findViewById(R.id.delete_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDeleteKeyLoc(context, id);
            }
        });
    }

    private void handleDeleteKeyLoc(final Context context, final int id) {
        Toast.makeText(mContext, "Deleting key location...", Toast.LENGTH_SHORT).show();
        AsyncExecutor.service.submit(new Runnable() {
            @Override
            public void run() {
                final int deleted = KeyLocationUtils.buildDeleteByIdCommand(context, id).call();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (deleted == 1) {
                            KeyLocationActivity.notifyOfKeyLocationChange(mActivity);
                            Toast.makeText(mActivity, "Key location deleted.", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(mActivity, "Key location couldn't be deleted :(",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}

package xyz.whereuat.whereuat;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.ListView;

import xyz.whereuat.whereuat.db.entry.PendingRequestEntry;
import xyz.whereuat.whereuat.ui.adapters.PendingRequestAdapter;

/**
 * This class manages the activity for displaying and interacting with the Pending Requests page.
 */
public class PendingRequestsActivity extends DrawerActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    PendingRequestAdapter mAdapter;
    MatrixCursor mCursor;

    @Override
    public void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.activity_pending_reqs);

        // TODO (julius): Delete this once pending request logic is implemented.
        mCursor = new MatrixCursor(PendingRequestEntry.COLUMNS);
        mCursor.addRow(new String[] {"Raymond Jacobson", "+1 (301) 467 2873", "0"});
        mCursor.addRow(new String[] {"Peter Kang", "+1 (917) 204 4747", "1"});
        mCursor.addRow(new String[] {null, "+1 (309) 319-0689", "2"});
        mCursor.addRow(new String[] {"Spencer Bovice Whitehead", "+1 (947) 772 2888", "3"});
        mCursor.addRow(new String[] {"Raymond Jacobson", "+1 (301) 467 2873", "0"});
        mCursor.addRow(new String[] {"Peter Kang", "+1 (917) 204 4747", "1"});
        mCursor.addRow(new String[] {null, "+1 (309) 319-0689", "2"});
        mCursor.addRow(new String[] {"Spencer Bovice Whitehead", "+1 (947) 772 2888", "3"});
        mCursor.addRow(new String[] {"Raymond Jacobson", "+1 (301) 467 2873", "0"});
        mCursor.addRow(new String[] {"Peter Kang", "+1 (917) 204 4747", "1"});
        mCursor.addRow(new String[] {null, "+1 (309) 319-0689", "2"});
        mCursor.addRow(new String[] {"Spencer Bovice Whitehead", "+1 (947) 772 2888", "3"});

        mAdapter = new PendingRequestAdapter(this, mCursor);
        ((ListView) findViewById(R.id.pending_reqs_list)).setAdapter(mAdapter);

        initDrawer();
    }

    /**
     * This is called when the loader is first made. It is necessary to implement the LoaderManager
     * callbacks.
     *
     * @param id the id of the loader, which is not needed because there is only one
     * @param args miscellaneous arguments to help make the loader, of which there are none
     * @return returns a new CursorLoader
     */
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, null, PendingRequestEntry.COLUMNS, null, null, null) {
            @Override
            public Cursor loadInBackground() { return mCursor; }
        };
    }

    /**
     * Swap the new cursor in. LoaderManager will take care of closing the old cursor. This is
     * needed to implement LoaderManager callbacks.
     *
     * @param loader the loader to apply the callbacks to
     * @param data the data to go into the loader
     */
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) { mAdapter.swapCursor(mCursor); }

    /**
     * This is called when the last Cursor provided to onLoadFinished() is about to be closed.
     * Swapping with null ensures the cursor is no longer being used.
     */
    public void onLoaderReset(Loader<Cursor> loader) { mAdapter.swapCursor(null); }
}

package xyz.whereuat.whereuat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ListView;

import xyz.whereuat.whereuat.db.entry.ContactRequestEntry;
import xyz.whereuat.whereuat.ui.adapters.ContactRequestAdapter;
import xyz.whereuat.whereuat.utils.ContactRequestUtils;

/**
 * This class manages the activity for displaying and interacting with pending contact requests.
 */
public class ContactRequestsActivity extends DrawerActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private ContactRequestAdapter mAdapter;
    private ReloadPendingRequestsReceiver mReloadPendingReqsReceiver;
    private IntentFilter mReloadPendingReqsFilter;

    @Override
    public void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.activity_contact_reqs);

        mAdapter = new ContactRequestAdapter(this);
        ((ListView) findViewById(R.id.contact_reqs_list)).setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(0, null, this);
        initDrawer(getString(R.string.contact_requests));

        mReloadPendingReqsReceiver = new ReloadPendingRequestsReceiver();
        mReloadPendingReqsFilter = new IntentFilter(Constants.RELOAD_CONTACT_REQS_BROADCAST);
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mReloadPendingReqsReceiver,
                mReloadPendingReqsFilter);
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReloadPendingReqsReceiver);
        super.onPause();
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
        return new CursorLoader(this, null, ContactRequestEntry.COLUMNS, null, null, null) {
            // loadInBackground executes the query (which selects all pending requests) in the
            // background, off the UI thread.
            @Override
            public Cursor loadInBackground() {
                return ContactRequestUtils.buildSelectAllCommand(ContactRequestsActivity.this)
                        .call();
            }
        };
    }

    /**
     * Swap the new cursor in. LoaderManager will take care of closing the old cursor. This is
     * needed to implement LoaderManager callbacks.
     *
     * @param loader the loader to apply the callbacks to
     * @param data the data to go into the loader
     */
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) { mAdapter.swapCursor(data); }

    /**
     * This is called when the last Cursor provided to onLoadFinished() is about to be closed.
     * Swapping with null ensures the cursor is no longer being used.
     */
    public void onLoaderReset(Loader<Cursor> loader) { mAdapter.swapCursor(null); }

    /**
     * This function is so other classes can notify this activity that the pending requests have
     * changed and should be reloaded.
     *
     * @param context the context that is notifying the change
     */
    public static void notifyOfContactRequestChange(Context context) {
        Intent intent = new Intent(Constants.RELOAD_CONTACT_REQS_BROADCAST);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * This receiver reloads the ListView of pending requests whenever it receives a broadcast.
     */
    class ReloadPendingRequestsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.swapCursor(ContactRequestUtils.buildSelectAllCommand(context).call());
        }
    }
}

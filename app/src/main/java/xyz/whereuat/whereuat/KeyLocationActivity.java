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
import android.view.View;
import android.widget.ListView;

import com.github.clans.fab.FloatingActionMenu;

import xyz.whereuat.whereuat.ui.adapters.KeyLocationAdapter;
import xyz.whereuat.whereuat.ui.views.KeyLocDialogFragment;
import xyz.whereuat.whereuat.utils.KeyLocationUtils;

/**
 * This class manages the activity for displaying and interacting with key locations.
 */
public class KeyLocationActivity extends DrawerActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "KeyLocationActivity";
    private KeyLocationAdapter mAdapter;
    private FloatingActionMenu mMenu;
    private ReloadKeyLocsReceiver mReloadKeyLocsReceiver;
    private IntentFilter mReloadKeyLocsFilter;

    @Override
    public void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.activity_key_locs);

        mMenu = (FloatingActionMenu) findViewById(R.id.menu);
        mAdapter = new KeyLocationAdapter(this);
        ((ListView) findViewById(R.id.key_locs_list)).setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
        initDrawer(getResources().getString(R.string.key_locations));

        mReloadKeyLocsReceiver = new ReloadKeyLocsReceiver();
        mReloadKeyLocsFilter = new IntentFilter(Constants.RELOAD_KEY_LOCS_BROADCAST);
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mReloadKeyLocsReceiver,
                mReloadKeyLocsFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReloadKeyLocsReceiver);
        mMenu.close(false);
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
        return new CursorLoader(this, null, null, null, null, null) {
            // loadInBackground executes the query (which selects all key locations) in the
            // background, off the UI thread.
            @Override
            public Cursor loadInBackground() {
                return KeyLocationUtils.buildSelectAllCommand(KeyLocationActivity.this).call();
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


    public void showKeyLocDialog(View view) {
        mMenu.close(true);
        new KeyLocDialogFragment().show(getFragmentManager(), TAG);
    }

    public static void notifyOfKeyLocationChange(Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(
                new Intent(Constants.RELOAD_KEY_LOCS_BROADCAST));
    }

    /**
     * This receiver reloads the GridView of contacts whenever it receives a new broadcast. This
     * class is necessary so other parts of the application can trigger a cursor reload so the UI
     * updates.
     */
    class ReloadKeyLocsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            mAdapter.swapCursor(KeyLocationUtils.buildSelectAllCommand(context).call());
        }
    }
}

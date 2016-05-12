package xyz.whereuat.whereuat.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;

import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.db.entry.ContactEntry;
import xyz.whereuat.whereuat.ui.views.ContactCard;


/**
 * Created by julius on 4/13/16.
 *
 * This class abstracts some of the overhead of creating a SimpleCursor adapter. It takes in only
 * a Context and passes it into the super constructor with the associated fields necessary to
 * correctly display the card. It also defines how the contact card view should be bound to data.
 */
public class ContactCardCursorAdapter extends SimpleCursorAdapter {
    private static final String TAG = "ContactCardCursor";
    // Each item in |bound_cols| should correspond to the item in |bound_views| where its data is
    // used.
    private static final String[] BOUND_COLS = new String[] {ContactEntry._ID};
    private static final int[] BOUND_VIEWS = new int[] {R.id.container_flipper};


    public ContactCardCursorAdapter(Context c) {
        super(c, R.layout.contact_card, null, BOUND_COLS, BOUND_VIEWS, 0);
    }

    /**
     * bindView should be overridden so it can pass data into the ContactCard to be set up and
     * displayed.
     *
     * @param view a ContactCard
     * @param context the calling Context
     * @param cursor a cursor filled with data to populate the view
     */
    @Override
    public void bindView (View view, Context context, Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(ContactEntry.COLUMN_NAME));
        boolean is_autoshared = cursor.getInt(cursor.getColumnIndex(
                ContactEntry.COLUMN_AUTOSHARE)) > 0;
        int color = cursor.getInt(cursor.getColumnIndex(ContactEntry.COLUMN_COLOR));
        int id = cursor.getInt(cursor.getColumnIndex(ContactEntry._ID));
        ((ContactCard) view).setData(name, is_autoshared, color, id);
    }
}


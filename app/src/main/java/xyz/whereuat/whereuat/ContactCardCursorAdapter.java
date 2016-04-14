package xyz.whereuat.whereuat;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;

import java.util.Random;

import xyz.whereuat.whereuat.db.entry.ContactEntry;


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
    private static final String[] BOUND_COLS = new String[] {ContactEntry.COLUMN_NAME,
            ContactEntry.COLUMN_NAME, ContactEntry.COLUMN_AUTOSHARE, ContactEntry.COLUMN_AUTOSHARE};
    private static final int[] BOUND_VIEWS = new int[] {R.id.front_view, R.id.back_view_fullname,
            R.id.auto_share_status, R.id.auto_share_button};


    public ContactCardCursorAdapter(Context c) {
        super(c, R.layout.contact_card, null, BOUND_COLS, BOUND_VIEWS, 0);

        // The ViewBinder should be overridden with a custom ViewBinder so it can define what to do
        // for views that are not text or need to have their text manipulated before being
        // displayed.
        setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(final View view, Cursor cursor, int col_index) {
                int vid = view.getId();
                switch (vid) {
                    // Set up the front of a card by setting the background color, name, and
                    // initials.
                    case R.id.front_view: {
                        String name = cursor.getString(col_index);
                        ((LatoTextView) view.findViewById(R.id.front_view_fullname)).setText(name);
                        ((LatoTextView) view.findViewById(R.id.front_view_initials)).setText(
                                getInitials(name));
                        view.setBackgroundColor(generateRandomColor());
                        break;
                    }
                    // Set the name of the contact on the back of the card.
                    case R.id.back_view_fullname: {
                        ((LatoTextView) view).setText(cursor.getString(col_index));
                        break;
                    }
                    // Fill the autoshare status on the front of the card if it needs to be filled.
                    case R.id.auto_share_status: {
                        ((AutoShareStar) view).setIsFilled(cursor.getInt(col_index) > 0);
                        break;
                    }
                    // Fill the autoshare button on the back of the card if it needs to be filled.
                    case R.id.auto_share_button: {
                        ((AutoShareStar) view).setIsFilled(cursor.getInt(col_index) > 0);
                        break;
                    }
                }
                return true;
            }
        });
    }

    private static int generateRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    // TODO: Flesh this function out once contacts are being pulled from the DB.
    private String getInitials(String name) {
        String[] names = name.split(" ");
        if (names.length == 2) {
            return String.format("%c%c", getFirstInitial(names[0]), getFirstInitial(names[1]));
        }
        return "XX";
    }

    private char getFirstInitial(String name) {
        if (name.length() < 0) return 'X';
        return Character.toUpperCase(name.charAt(0));
    }
}


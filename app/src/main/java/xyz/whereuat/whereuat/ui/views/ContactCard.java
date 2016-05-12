package xyz.whereuat.whereuat.ui.views;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import xyz.whereuat.whereuat.AsyncExecutor;
import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.db.entry.ContactEntry;
import xyz.whereuat.whereuat.ui.animations.AnimationFactory;
import xyz.whereuat.whereuat.utils.ContactUtils;
import xyz.whereuat.whereuat.utils.HttpRequestHandler;
import xyz.whereuat.whereuat.utils.PreferenceController;

/**
 * This class is a wrapper around a ViewFlipper to provide default functionality for a contact card
 * in the contacts GridView. It sets the ViewFlipper to be a square and sets its click listeners.
 */
public class ContactCard extends ViewFlipper {
    private static final String TAG = "ContactCard";

    public ContactCard(final Context context, AttributeSet attrs) {
        super(context, attrs);
        // Set up the long click listener to flip the card.
        this.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View click) {
                AnimationFactory.flipTransition(ContactCard.this,
                        AnimationFactory.FlipDirection.RIGHT_LEFT);
                return true;
            }
        });
        // Set up the normal click listener to just log the location.
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View click) {
                // Make a Toast to provide user feedback that a request was at least attempted.
                Toast.makeText(context, "Sending whereu@...", Toast.LENGTH_LONG).show();

                // The back view shouldn't do anything if it's clicked.
                if (ContactCard.this.getCurrentView().getId() == R.id.back_view)
                    return;

                AsyncExecutor.service.submit(new Runnable() {
                    @Override
                    public void run() {
                        // Select the phone number of the contact with this id.
                        String contact_id = ContactCard.this.getTag().toString();
                        Cursor phone = ContactUtils.buildSelectContactByIdCommand(context,
                                contact_id, new String[] {ContactEntry.COLUMN_PHONE}).call();

                        // Get the contact's phone number from the cursor and send them an @request.
                        if (phone.moveToFirst()) {
                            String to_phone = phone.getString(
                                    phone.getColumnIndex(ContactEntry.COLUMN_PHONE));

                            (new HttpRequestHandler(context)).postAtRequest(
                                    (new PreferenceController(context)).getClientPhoneNumber(),
                                    to_phone,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            String text = "whereu@ sent!";
                                            Toast.makeText(context, text, Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.d(TAG, "Error sending the @request POST " +
                                                    error.toString());
                                        }
                                    }
                            );
                        }
                    }
                });
            }
        });
    }

    // Override onMeasure to set the contact card to always be a square based on the width. The
    // GridView that the ContactCard lives in should constrain the width of the card so that is the
    // dimension to use for the square.
    @Override
    protected void onMeasure(int width_measure_spec, int height_measure_spec) {
        super.onMeasure(width_measure_spec, width_measure_spec);
    }

    /**
     * This function sets the data for the view and binds it to the proper inner views of the card.
     *
     * @param name a String for the name of the contact this card represents
     * @param is_autoshared a boolean for the autoshare status of this contact
     * @param color the color for this card
     * @param id the id of the contact represented by this card, for use as the view's tag
     */
    public void setData(String name, boolean is_autoshared, int color, int id) {
        // Set up the front of a card by setting the background color, name, and
        // initials.
        View front_view = this.findViewById(R.id.front_view);
        ((LatoTextView) front_view.findViewById(R.id.front_view_fullname)).setText(name);
        ((LatoTextView) front_view.findViewById(R.id.front_view_initials)).setText(
                getInitials(name));
        front_view.setBackgroundColor(color);

        // Set the name of the contact on the back of the card.
        ((LatoTextView) this.findViewById(R.id.back_view_fullname)).setText(name);

        // Fill the autoshare status on the front of the card if it needs to be filled.
        ((AutoShareStar) this.findViewById(R.id.auto_share_status)).setAutoShare(is_autoshared);

        // Fill the autoshare button on the back of the card if it needs to be filled.
        ((AutoShareStar) this.findViewById(R.id.auto_share_button)).setAutoShare(is_autoshared);

        // Set the tag on the ContactCard to be the contact's id in the database so the
        // id can be used later to query the database.
        this.setTag(id);
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

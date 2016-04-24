package xyz.whereuat.whereuat;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import xyz.whereuat.whereuat.db.DbTask;
import xyz.whereuat.whereuat.db.command.QueryCommand;
import xyz.whereuat.whereuat.db.entry.ContactEntry;

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
                // The back view shouldn't do anything if it's clicked.
                if (ContactCard.this.getCurrentView().getId() == R.id.back_view)
                    return;

                // Select the phone number of the contact with this id.
                String selection = String.format("%s=?", ContactEntry._ID);
                String[] selection_args = new String[] {ContactCard.this.getTag().toString()};
                QueryCommand query = new QueryCommand(context, ContactEntry.TABLE_NAME, true,
                        new String[] {ContactEntry.COLUMN_PHONE}, selection, selection_args, null,
                        null, null, null);

                new DbTask() {
                    @Override
                    public void onPostExecute(Object result) {
                        // Get the contact's phone number from the cursor and send them an @request.
                        if (((Cursor) result).moveToFirst()) {
                            String to_phone = ((Cursor) result).getString(
                                    ((Cursor) result).getColumnIndex(ContactEntry.COLUMN_PHONE));

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
                }.execute(query);
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
}

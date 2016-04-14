package xyz.whereuat.whereuat;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ViewFlipper;

/**
 * This class is a wrapper around a ViewFlipper to provide default functionality for a contact card
 * in the contacts GridView. It sets the ViewFlipper to be a square and sets its click listeners.
 */
public class ContactCard extends ViewFlipper {
    private static final String TAG = "ContactCard";

    public ContactCard(Context c, AttributeSet attrs) {
        super(c, attrs);
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
        // TODO: This should send an @request.
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View click) {
                Location loc = LocationProviderService.getLocation();
                try {
                    Log.d(TAG, loc.getLatitude() + " " + loc.getLongitude());
                } catch (NullPointerException e) {
                    Log.d(TAG, "Null location");
                }
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

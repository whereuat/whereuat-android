package xyz.whereuat.whereuat.ui.views;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import xyz.whereuat.whereuat.AsyncExecutor;
import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.db.entry.ContactEntry;
import xyz.whereuat.whereuat.utils.ContactUtils;

/**
 * Created by julius on 3/22/16.
 *
 * This class is for encapsulating all of the functionality of a the autoshare star. If the star is
 * clickable then it is on the back of a ContactCard, will update its contact's autoshare status
 * when clicked, and is drawn with an outline. If the star is not clickable then it is on the front
 * of a ContactCard and is filled in.
 */
public class AutoShareStar extends View {
    private int mColor;
    private Context mContext;
    private boolean mIsAutoShared;
    private boolean mIsClickable;
    private Paint mPaint;
    private Path mPath;
    private static final String TAG = "AutoShareStar";

    /**
     * The constructor pulls values out of attrs in order to set member variables.
     *
     * @param context the calling Context
     * @param attrs a set of attributes for if the star is filled and/or clickable
     */
    public AutoShareStar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AutoShareStar);
        try {
            mColor = ta.getColor(R.styleable.AutoShareStar_draw_color,
                                 ContextCompat.getColor(context, R.color.offWhite));
            mIsAutoShared = ta.getBoolean(R.styleable.AutoShareStar_is_filled, true);
            mIsClickable = ta.getBoolean(R.styleable.AutoShareStar_is_clickable, false);
        } finally {
            ta.recycle();
        }
        mPaint = new Paint();
        mPath = new Path();
    }

    public void toggleAutoShare() {
        mIsAutoShared = !mIsAutoShared;
        redraw();
    }

    private void redraw() {
        postInvalidate();
        requestLayout();
    }

    /**
     * When clicked, the contact's autoshare status will be updated and this star will be redrawn
     * along with the star on the other side of the contact's ContactCard.
     *
     * @param event a MotionEvent on the card
     * @return always returns true
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Only trigger the event handler if the star is clickable
        if(event.getAction() == MotionEvent.ACTION_UP && mIsClickable) {
            // Execute the update query.
            AsyncExecutor.service.submit(new Runnable() {
                @Override
                public void run() {
                    Activity activity = (Activity) AutoShareStar.this.getContext();

                    // Construct the query for updating the correct contact in the database.
                    ContentValues updated_val = new ContentValues();
                    updated_val.put(ContactEntry.COLUMN_AUTOSHARE, !mIsAutoShared);

                    String where = String.format("%s=?", ContactEntry._ID);
                    final ContactCard contact_card = (ContactCard) AutoShareStar.this.getParent()
                            .getParent().getParent();
                    String contact_id = contact_card.getTag().toString();
                    String[] where_args = new String[] {contact_id};
                    Integer result = ContactUtils.buildUpdateCommand(mContext, updated_val,
                            where, where_args).call();

                    if (result == 1) {
                        Log.d(TAG, "Successful autoshare update");
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AutoShareStar.this.toggleAutoShare();
                                // Update the AutoShareStar on the other side of the ContactCard.
                                ((AutoShareStar) contact_card.findViewById(R.id.auto_share_status))
                                        .toggleAutoShare();
                            }
                        });
                    } else {
                        Log.d(TAG, "Something went wrong with the AutoShare update.");
                    }
                }
            });
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // (!mIsClickable && !mIsAutoShared) Means this is a star on the front of the contact card
        // that should be invisible. Rather than setting the visibility of the AutoShareStar to
        // INVISIBLE, just don't draw the star. This prevents issues with redrawing.
        if (!mIsClickable && !mIsAutoShared) {
            super.onDraw(canvas);
            return;
        }

        mPaint.setColor(mColor);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(mIsAutoShared ? Paint.Style.FILL : Paint.Style.STROKE);

        float width = getWidth();
        float height = getHeight();

        // X and Y coordinates for the bottom of the top point on the star.
        float neckY = (float) 0.34;
        float neckX = (float) 0.656;
        // Y coordinate for the point of the left point of the star.
        float armY = (float) 0.393;
        // X and Y coordinates for the bottom of the left point of the star.
        float pitY = (float) 0.648;
        float pitX = (float) 0.753;
        // Y coordinate for the crease between the bottom two points on the star.
        float crotchY = (float) 0.834;
        // X coordinate for the bottom left point on the star.
        float footX = (float) 0.807;
        float mid = (float) 0.5;

        mPath.moveTo(mid * width, 0);

        // Draw the left side, starting at the top point and going counter-clockwise to the crotch.
        mPath.lineTo(neckX * width, neckY * height);
        mPath.lineTo(width, armY * height);
        mPath.lineTo(pitX * width, pitY * height);
        mPath.lineTo(footX * width, height);

        mPath.lineTo(mid * width, crotchY * height);

        // Draw the right side, starting at the crotch and going counter-clockwise to the top point.
        mPath.lineTo((1-footX) * width, height);
        mPath.lineTo((1-pitX) * width, pitY * height);
        mPath.lineTo(0, armY * height);
        mPath.lineTo((1-neckX) * width, neckY * height);

        mPath.lineTo(mid * width, 0);

        mPath.close();
        canvas.drawPath(mPath, mPaint);

        super.onDraw(canvas);
    }

    /**
     * Set the value of the star. The star should be redrawn otherwise issues arise when reusing the
     * star's view.
     *
     * @param autoShare a boolean for if this star represents an autoshared contact
     */
    public void setAutoShare(boolean autoShare) {
        mIsAutoShared = autoShare;
        redraw();
    }
}

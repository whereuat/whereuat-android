package xyz.whereuat.whereuat;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import xyz.whereuat.whereuat.db.DbTask;
import xyz.whereuat.whereuat.db.command.UpdateCommand;
import xyz.whereuat.whereuat.db.entry.ContactEntry;

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
        postInvalidate();
        requestLayout();
    }

    /*
        When clicked, the contact's autoshare status will be updated and this star will be redrawn
        along with the star on the other side of the contact's ContactCard.
     */
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP && mIsClickable) {
            ContentValues updated_val = new ContentValues();
            updated_val.put(ContactEntry.COLUMN_AUTOSHARE, !mIsAutoShared);

            String where = String.format("%s=?", ContactEntry._ID);
            final ContactCard contact_card = (ContactCard) this.getParent().getParent().getParent();
            String contact_id = contact_card.getTag().toString();
            String[] where_args = new String[] {contact_id};
            UpdateCommand update = new UpdateCommand(mContext, ContactEntry.TABLE_NAME, updated_val,
                    where, where_args);

            new DbTask() {
                @Override
                public void onPostExecute(Object result) {
                    if (((Integer) result) == 1) {
                        Log.d(TAG, "Successful autoshare update");
                        AutoShareStar.this.toggleAutoShare();
                        // Update the AutoShareStar on the other side of the ContactCard.
                        ((AutoShareStar) contact_card.findViewById(R.id.auto_share_status))
                                .toggleAutoShare();
                    } else {
                        Log.d(TAG, "Something went wrong with the AutoShare update.");
                    }
                }
            }.execute(update);
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

        float neckY = (float) 0.34;
        float neckX = (float) 0.656;
        float armY = (float) 0.393;
        float pitY = (float) 0.648;
        float pitX = (float) 0.753;
        float crotchY = (float) 0.834;
        float footX = (float) 0.807;
        float mid = (float) 0.5;

        mPath.moveTo(mid * width, 0);

        // Draw the left side
        mPath.lineTo(neckX * width, neckY * height);
        mPath.lineTo(width, armY * height);
        mPath.lineTo(pitX * width, pitY * height);
        mPath.lineTo(footX * width, height);

        mPath.lineTo(mid * width, crotchY * height);

        // Draw the right side
        mPath.lineTo((1-footX) * width, height);
        mPath.lineTo((1-pitX) * width, pitY * height);
        mPath.lineTo(0, armY * height);
        mPath.lineTo((1-neckX) * width, neckY * height);

        mPath.lineTo(mid * width, 0);

        mPath.close();
        canvas.drawPath(mPath, mPaint);

        super.onDraw(canvas);
    }

    public void setAutoShare(boolean autoShare) {
        mIsAutoShared = autoShare;
    }
}

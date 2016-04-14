package xyz.whereuat.whereuat;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by julius on 3/22/16.
 */
public class AutoShareStar extends View {
    private int mColor;
    private boolean mIsFilled;
    private boolean mIsClickable;
    private Paint mPaint;
    private Path mPath;

    public AutoShareStar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AutoShareStar);
        try {
            mColor = ta.getColor(R.styleable.AutoShareStar_draw_color,
                                 ContextCompat.getColor(context, R.color.offWhite));
            mIsFilled = ta.getBoolean(R.styleable.AutoShareStar_is_filled, true);
            mIsClickable = ta.getBoolean(R.styleable.AutoShareStar_is_clickable, false);
        } finally {
            ta.recycle();
        }
        mPaint = new Paint();
        mPath = new Path();
    }

    public void toggleFilled() {
        mIsFilled = !mIsFilled;
        postInvalidate();
        requestLayout();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP && mIsClickable){
            this.toggleFilled();
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mColor);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(mIsFilled ? Paint.Style.FILL : Paint.Style.STROKE);

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

    public void setIsFilled(boolean filled) {
        mIsFilled = filled;
    }
}

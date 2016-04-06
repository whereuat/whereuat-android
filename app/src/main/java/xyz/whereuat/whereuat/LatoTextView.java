package xyz.whereuat.whereuat;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * Created by julius on 3/24/16.
 */
public class LatoTextView extends TextView {
    public static final int BOLD = 0;
    public static final int REGULAR= 1;
    public static final int SEMIBOLD = 2;

    public LatoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface tf;
        TypedArray ta = context.obtainStyledAttributes(attrs, xyz.whereuat.whereuat.R.styleable.LatoTextView);
        switch (ta.getInt(xyz.whereuat.whereuat.R.styleable.LatoTextView_lato_font, 0)) {
            case BOLD:
                tf = Typeface.createFromAsset(context.getAssets(), "Lato-Bold.ttf");
                break;
            case REGULAR:
                tf = Typeface.createFromAsset(context.getAssets(), "Lato-Regular.ttf");
                break;
            case SEMIBOLD:
                tf = Typeface.createFromAsset(context.getAssets(), "Lato-Semibold.ttf");
                break;
            default:
                tf = Typeface.createFromAsset(context.getAssets(), "Lato-Regular.ttf");
        }
        setTypeface(tf);
    }
}

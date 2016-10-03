package xyz.whereuat.whereuat.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import xyz.whereuat.whereuat.R;

/**
 * Created by kangp3 on 10/2/16.
 */

public class LatoUtils {
    public static final int BOLD = 0;
    public static final int REGULAR= 1;
    public static final int SEMIBOLD = 2;
    public static final int LIGHT = 3;

    public static Typeface getFontStyle(Context context, AttributeSet attrs) {
        Typeface tf;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LatoTextView);
        // Switch over the different styles of Lato and then set the text to the style defined in
        // the XML attributes of the view.
        switch (ta.getInt(R.styleable.LatoTextView_lato_font, 0)) {
            case BOLD:
                tf = Typeface.createFromAsset(context.getAssets(), "Lato-Bold.ttf");
                break;
            case REGULAR:
                tf = Typeface.createFromAsset(context.getAssets(), "Lato-Regular.ttf");
                break;
            case SEMIBOLD:
                tf = Typeface.createFromAsset(context.getAssets(), "Lato-Semibold.ttf");
                break;
            case LIGHT:
                tf = Typeface.createFromAsset(context.getAssets(), "Lato-Light.ttf");
                break;
            default:
                tf = Typeface.createFromAsset(context.getAssets(), "Lato-Regular.ttf");
        }
        ta.recycle();
        return tf;
    }
}

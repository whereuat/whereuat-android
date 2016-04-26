package xyz.whereuat.whereuat.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import xyz.whereuat.whereuat.R;


/**
 * Created by julius on 3/24/16.
 *
 * This class wraps a TextView and makes it easier to set the style of text within the view. Its
 * main purpose is to provide access to an enum for different styles of the Lato font.
 */
public class LatoTextView extends TextView {
    public static final int BOLD = 0;
    public static final int REGULAR= 1;
    public static final int SEMIBOLD = 2;

    public LatoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
            default:
                tf = Typeface.createFromAsset(context.getAssets(), "Lato-Regular.ttf");
        }
        setTypeface(tf);
        ta.recycle();
    }
}

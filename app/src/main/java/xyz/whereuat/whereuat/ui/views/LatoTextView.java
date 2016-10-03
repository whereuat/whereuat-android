package xyz.whereuat.whereuat.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import xyz.whereuat.whereuat.utils.LatoUtils;


/**
 * Created by julius on 3/24/16.
 *
 * This class wraps a TextView and makes it easier to set the style of text within the view. Its
 * main purpose is to provide access to an enum for different styles of the Lato font.
 */
public class LatoTextView extends TextView {
    public LatoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface tf = LatoUtils.getFontStyle(context, attrs);
        setTypeface(tf);
    }

}

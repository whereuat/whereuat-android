package xyz.whereuat.whereuat.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import xyz.whereuat.whereuat.utils.LatoUtils;

/**
 * Created by kangp3 on 6/1/16.
 */
public class LatoEditTextView extends EditText {
    public LatoEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface tf = LatoUtils.getFontStyle(context, attrs);
        setTypeface(tf);
    }
}

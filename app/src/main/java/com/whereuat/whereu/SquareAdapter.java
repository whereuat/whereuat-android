package com.whereuat.whereu;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.Random;

/**
 * Created by whites5 on 2/24/16.
 */
public class SquareAdapter extends  BaseAdapter{
    private Context mContext;
    private final String[] vals = new String[] {"Spencer", "Julius", "Peter", "Ray", "Spetelius"};

    public SquareAdapter(Context c) {
        mContext = c;
    }

    int fID = 0;

    public int generateViewId(View v) {
        while( v.findViewById(++fID) != null );
        return fID;
    }

    @Override
    public int getCount() {
        return vals.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    private int calculateIdealSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        // in dp
        double idealSize = 182.5;
        float density = mContext.getResources().getDisplayMetrics().density;
        int screenWidth = Math.round(metrics.widthPixels/density);
        int numSquares = (int) Math.round(screenWidth/idealSize);
        return (int) (screenWidth*density)/numSquares;
    }

    protected int generateRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewFlipper flipper;
        if (convertView == null) {
            flipper = (ViewFlipper) inflater.inflate(R.layout.grid_item_container, null);
//            flipper.getDisplayedChild();
            View v = flipper.findViewById(R.id.front_view);
            v.setBackgroundColor(generateRandomColor());
            TextView textView = (TextView) v.findViewById(R.id.front_view_text);
            textView.setText(vals[position]);

            int ideal_size = calculateIdealSize();
            flipper.setLayoutParams(new GridView.LayoutParams(ideal_size, ideal_size));
            flipper.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View click) {
                    AnimationFactory.flipTransition(flipper,
                            AnimationFactory.FlipDirection.LEFT_RIGHT);
                    return true;
                }
            });
        } else {
            flipper = (ViewFlipper) convertView;
        }

        return flipper;
    }

//    private Canvas drawStar()
}

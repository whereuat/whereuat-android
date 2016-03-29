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
    private final String[] vals = new String[] {"Spencer Whitehead", "Julius Alexander IV",
                                                "Peter Kang", "Ray Shu Jacobson",
                                                "Spetelius Bartlebee Cayenne Ray",
                                                "Spencer Whitehead", "Julius Alexander IV",
                                                "Peter Kang", "Ray Shu Jacobson"};

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
            setupFront(flipper.findViewById(R.id.front_view), vals[position]);
            setupBack(flipper.findViewById(R.id.back_view), vals[position]);

            int ideal_size = calculateIdealSize();
            flipper.setLayoutParams(new GridView.LayoutParams(ideal_size, ideal_size));
            flipper.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View click) {
                    AnimationFactory.flipTransition(flipper,
                            AnimationFactory.FlipDirection.RIGHT_LEFT);
                    return true;
                }
            });
        } else {
            flipper = (ViewFlipper) convertView;
        }

        return flipper;
    }

    private void setupFront(View v, String name) {
        v.setBackgroundColor(generateRandomColor());
        // TODO: Only here for example. Remove once mock data is in the DB and functional.
        AutoShareStar star = (AutoShareStar) v.findViewById(R.id.auto_share_status);
        if (name.length()%2 == 0)
            star.setVisibility(View.INVISIBLE);

        // Set up the initials TextView
        ((TextView) v.findViewById(R.id.front_view_initials)).setText(getInitials(name));

        // Set up the fullname TextView
        ((TextView) v.findViewById(R.id.front_view_fullname)).setText(name);
    }

    private void setupBack(View v, String name) {
        // TODO: Only here for example. Remove once mock data is in the DB and functional.
        AutoShareStar star = (AutoShareStar) v.findViewById(R.id.auto_share_button);
        if (name.length()%2 == 0)
            star.toggleFilled();

        // Set up the fullname TextView
        ((TextView) v.findViewById(R.id.back_view_fullname)).setText(name);
    }

    // TODO: Flesh this function out once contacts are being pulled from the DB.
    private String getInitials(String name) {
        String[] names = name.split(" ");
        if (names.length == 2) {
            return String.format("%c%c", getFirstInitial(names[0]), getFirstInitial(names[1]));
        }
        return "XX";
    }

    private char getFirstInitial(String name) {
        if (name.length() < 0) return 'X';
        return Character.toUpperCase(name.charAt(0));
    }
}

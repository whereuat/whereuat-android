package xyz.whereuat.whereuat.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import xyz.whereuat.whereuat.R;
import xyz.whereuat.whereuat.utils.DrawerItem;

/**
 * A custom adapter for holding items in the slide out drawer.
 */
public class DrawerListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<DrawerItem> mDrawerItems;

    public DrawerListAdapter(Context context, ArrayList<DrawerItem> items){
        this.mContext = context;
        this.mDrawerItems = items;
    }

    @Override
    public int getCount() { return mDrawerItems.size(); }

    @Override
    public Object getItem(int position) { return mDrawerItems.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convert_view, ViewGroup parent) {
        if (convert_view == null) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convert_view = inflater.inflate(R.layout.drawer_list_item, parent, false);
        }

        ImageView icon = (ImageView) convert_view.findViewById(R.id.drawer_list_item_icon);
        TextView title = (TextView) convert_view.findViewById(
                R.id.drawer_list_item_title);

        icon.setImageResource(mDrawerItems.get(position).getIcon());
        title.setText(mDrawerItems.get(position).getTitle());

        return convert_view;
    }
}

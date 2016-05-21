package xyz.whereuat.whereuat;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import xyz.whereuat.whereuat.ui.adapters.DrawerListAdapter;
import xyz.whereuat.whereuat.ui.views.LatoTextView;
import xyz.whereuat.whereuat.utils.DrawerItem;
import xyz.whereuat.whereuat.utils.PreferenceController;

/**
 * A class to be extended by any activities that use the toolbar and drawer. This class sets up the
 * drawer and ties it to the toolbar.
 */
public class DrawerActivity extends AppCompatActivity {
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
    }

    /**
     * Creates the slide-out drawer and populates it with list items.
     *
     * Note: This function must be called by a subclass in order to properly initialize the drawer.
     *       The initDrawer logic cannot go into onCreate because the activity will not have its
     *       content view set yet, which is necessary for findViewById calls.
     */
    protected void initDrawer(String toolbar_title) {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        ((LatoTextView) mToolbar.findViewById(R.id.toolbar_title)).setText(toolbar_title);
        setSupportActionBar(mToolbar);

        ((LatoTextView) findViewById(R.id.drawer_phone)).setText(
                new PreferenceController(this).getClientPhoneNumber());

        String[] menu_titles = getResources().getStringArray(R.array.drawer_items);
        TypedArray menu_icons = getResources().obtainTypedArray(R.array.drawer_icons);

        // Fill the list with all of the items to be displayed.
        ArrayList<DrawerItem> drawer_items = new ArrayList<>();
        for (int i = 0; i < menu_icons.length(); ++i) {
            drawer_items.add(new DrawerItem(menu_titles[i], menu_icons.getResourceId(i, -1)));
        }

        ListView items = ((ListView) findViewById(R.id.drawer_list));
        items.setAdapter(new DrawerListAdapter(this, drawer_items));

        // Setting the click listener here rather than in the DrawerListAdapter keeps the animation
        // on the click.
        items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position) {
                    case 0:
                        DrawerActivity.this.startActivity(
                                new Intent(DrawerActivity.this, MainActivity.class));
                        break;
                    case 2:
                        DrawerActivity.this.startActivity(
                                new Intent(DrawerActivity.this, ContactRequestsActivity.class));
                }
            }
        });

        // There must be a DrawerLayout with the id "drawer_layout" in the activity's layout.
        DrawerLayout drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawer_toggle = new ActionBarDrawerToggle(this, drawer_layout,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer_layout.setDrawerListener(drawer_toggle);

        drawer_toggle.syncState();
        menu_icons.recycle();
    }
}

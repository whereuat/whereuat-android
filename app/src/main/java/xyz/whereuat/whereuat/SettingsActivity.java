package xyz.whereuat.whereuat;

import android.os.Bundle;

/**
 * Created by kangp3 on 5/19/16.
 */
public class SettingsActivity extends DrawerActivity {
    @Override
    public void onCreate(Bundle saved_instance_state) {
        super.onCreate(saved_instance_state);
        setContentView(R.layout.activity_settings);

        initDrawer(getString(R.string.settings));
    }
}

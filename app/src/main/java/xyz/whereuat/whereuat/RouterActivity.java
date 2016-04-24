package xyz.whereuat.whereuat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import xyz.whereuat.whereuat.utils.PreferenceController;

/**
 * Created by julius on 3/25/16.
 *
 * This is a blank activity that only serves to route the application to the login or contacts
 * screen based on whether the user has an account already or not.
 */
public class RouterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceController prefs = new PreferenceController(this);
        this.startActivity(new Intent(this, prefs.hasAccount() ? MainActivity.class :
                                                                 LoginActivity.class));
        this.finish();
    }
}

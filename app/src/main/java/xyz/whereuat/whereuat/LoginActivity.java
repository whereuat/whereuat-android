package xyz.whereuat.whereuat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.whereuat.whereuat.gcm.RegistrationIntentService;
import xyz.whereuat.whereuat.utils.HttpRequestHandler;
import xyz.whereuat.whereuat.utils.PreferenceController;


/**
 * This class is used to handle all views and logic associated with creating and verifying a new
 * account.
 */
public class LoginActivity extends AppCompatActivity {
    private String TAG = "LoginActivity";
    private EditText mPhoneEdit;
    private EditText mVerifyCode;
    private PreferenceController mPrefs;
    private View[] mAccountRequestSection;
    private View[] mAccountCreateSection;
    private View mCreateBtn;
    private View mProgressWheel;
    private HttpRequestHandler mHttpReqHandler;
    private TokenBroadcastReceiver mTokenReceiver;
    private IntentFilter mTokenFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up some member variables for objects that the activity regularly needs.
        mPrefs = new PreferenceController(this);
        mPhoneEdit = (EditText) findViewById(R.id.phone_number_input);
        mVerifyCode = (EditText) findViewById(R.id.verification_code_input);
        mAccountRequestSection = new View[] {findViewById(R.id.phone_number_prompt),
                findViewById(R.id.account_request_btn_layout)};
        mAccountCreateSection = new View[] {findViewById(R.id.verification_code_prompt),
               findViewById(R.id.account_create_btn_layout)};
        mCreateBtn = findViewById(R.id.account_create_btn);
        mProgressWheel = findViewById(R.id.account_create_progress_wheel);
        mHttpReqHandler = new HttpRequestHandler(this);

        mTokenFilter = new IntentFilter(Constants.TOKEN_BROADCAST);
        mTokenReceiver = new TokenBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mTokenReceiver, mTokenFilter);

        if (mPrefs.isWaitingForVerify())
            showCreateHideRequest();
        else
            showRequestHideCreate();
    }

    /**
     * This function should be overridden so the receiver can be unregistered when it is not needed.
     */
    @Override
    protected void onPause() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mTokenReceiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Unable to unregister the TokenReceiver");
        }
        super.onPause();
    }

    /**
     * This function should be overridden so the receiver can be registered when it is needed.
     */
    @Override
    public void onResume() {
        try {
            LocalBroadcastManager.getInstance(this).registerReceiver(mTokenReceiver, mTokenFilter);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Unable to register the TokenReceiver");
        }
        super.onResume();
    }

    /**
     * If the phone number is valid, sends an account request to the server and changes the UI to
     * the verification view.
     *
     * @param v unused, only here so the function can be bound in the XML file
     */
    public void requestAccount(View v) {
        showButtonHideWheel();
        String phone_number = mPhoneEdit.getText().toString();
        if (isValidPhoneForm(phone_number)) {
            mHttpReqHandler.postAccountRequest(phone_number,
                    // If the response is successful, update the user's preference to show that they
                    // are waiting for their account to be verified and show the verification text
                    // input.
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mPrefs.setWaitingForVerifyPref(true);
                            showCreateHideRequest();
                        }
                    },
                    // If there was an error, update the user's preference to show that they are not
                    // waiting to verify their account and make sure the phone number text input is
                    // shown.
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String text = "Error making your account request :(";
                            Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT).show();
                            showRequestHideCreate();
                            mPrefs.setWaitingForVerifyPref(false);
                        }
                    });
            mPrefs.setClientPhoneNumberPref(phone_number);
        } else {
            // TODO: There's a better way to handle this because the user shouldn't really have to
            // put the string in this form.
            String text = "The number should be in the form '+1xxxxxxxxxx'.";
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This function shows the text input for entering the user's phone number and hides the text
     * input for entering the verification code.
     */
    private void showCreateHideRequest() {
        Animation out_anim = AnimationUtils.loadAnimation(this, R.anim.out_to_left);
        Animation in_anim = AnimationUtils.loadAnimation(this, R.anim.in_from_right);

        for (View v : mAccountCreateSection) {
            v.startAnimation(in_anim);
            v.setVisibility(View.VISIBLE);
        }

        for (View v : mAccountRequestSection) {
            v.startAnimation(out_anim);
            v.setVisibility(View.GONE);
        }
    }

    /**
     * This function shows the text input for entering the verification code and hides the text
     * input for entering the user's phone number.
     */
    private void showRequestHideCreate() {
        Animation out_anim = AnimationUtils.loadAnimation(this, R.anim.out_to_right);
        Animation in_anim = AnimationUtils.loadAnimation(this, R.anim.in_from_left);

        for (View v : mAccountRequestSection) {
            v.startAnimation(in_anim);
            v.setVisibility(View.VISIBLE);
        }

        for (View v : mAccountCreateSection) {
            v.startAnimation(out_anim);
            v.setVisibility(View.GONE);
        }
    }

    private void showButtonHideWheel() {
        mProgressWheel.setVisibility(View.GONE);
        mCreateBtn.setVisibility(View.VISIBLE);
    }

    private void showWheelHideButton() {
        mProgressWheel.setVisibility(View.VISIBLE);
        mProgressWheel.setAlpha(0f);
        mProgressWheel.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null);

        mCreateBtn.animate()
                .alpha(0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCreateBtn.setVisibility(View.GONE);
                        mCreateBtn.setAlpha(1f);
                    }
                });
    }

    /**
     * Displays the transition wheel and starts the RegistrationIntentService.
     *
     * @param v unused, only here so the function can be bound in the XML file
     */
    public void createNewAccount(View v) {
        showWheelHideButton();
        startService(new Intent(this, RegistrationIntentService.class));
    }

    /**
     * Checks that the phone number is in a valid form.
     *
     * @param phone_number the phone number to check for validity
     * @return returns true if the phone number is a valid form
     */
    private boolean isValidPhoneForm(String phone_number) {
        String pattern = "\\+1\\d{10}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(phone_number);
        return m.find();
    }

    /**
     * A receiver for handling token generation. This receiver will trigger the new account request
     * after generating the GCM token.
     */
    private class TokenBroadcastReceiver extends BroadcastReceiver {
        private HashSet<Integer> received_ids = new HashSet<>();

        public void onReceive(Context context, Intent intent) {
            // Check the unique broadcast ID. If it is a repeat, ignore the broadcast.
            Integer broadcast_id = intent.getIntExtra(Constants.TOKEN_BROADCAST_ID_EXTRA, -1);
            if (received_ids.contains(broadcast_id) || broadcast_id == 0)
                return;
            received_ids.add(broadcast_id);

            String token = intent.getStringExtra(Constants.TOKEN_EXTRA);
            mHttpReqHandler.postAccountNew(mPrefs.getClientPhoneNumber(), token,
                    mVerifyCode.getText().toString(),
                    // If there is a successful response, update the user's preferences and then
                    // show the main screen for requesting contact location.
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mPrefs.setHasAccountPref(true);
                            mPrefs.setWaitingForVerifyPref(false);
                            LoginActivity.this.startActivity(
                                    new Intent(LoginActivity.this, MainActivity.class));
                            LoginActivity.this.finish();
                        }
                    },
                    // If there is an error, show the phone number text input.
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String text = "Error making your account :(";
                            Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT).show();
                            showRequestHideCreate();
                        }
                    });
        }
    }
}

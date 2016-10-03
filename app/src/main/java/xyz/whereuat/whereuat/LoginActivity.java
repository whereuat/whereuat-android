package xyz.whereuat.whereuat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.whereuat.whereuat.gcm.RegistrationIntentService;
import xyz.whereuat.whereuat.utils.HttpRequestHandler;
import xyz.whereuat.whereuat.utils.PhonebookUtils;
import xyz.whereuat.whereuat.utils.PreferenceController;


/**
 * This class is used to handle all views and logic associated with creating and verifying a new
 * account.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int AREA_CODE_LENGTH = 3;
    private static final int LINE_NUMBER_LENGTH = 7;

    private EditText mAreaEdit;
    private EditText mLineEdit;
    private EditText mVerifyCode;
    private PreferenceController mPrefs;
    private View[] mAccountRequestSection;
    private View[] mAccountCreateSection;
    private HttpRequestHandler mHttpReqHandler;
    private TokenBroadcastReceiver mTokenReceiver;
    private IntentFilter mTokenFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up some member variables for objects that the activity regularly needs.
        mPrefs = new PreferenceController(this);
        mAreaEdit = (EditText) findViewById(R.id.area_code_input);
        mLineEdit = (EditText) findViewById(R.id.line_number_input);
        setAreaEditListeners();
        setLineEditListeners();

        mVerifyCode = (EditText) findViewById(R.id.verification_code_input);
        mAccountRequestSection = new View[] {findViewById(R.id.phone_number_prompt),
                findViewById(R.id.account_request_btn)};
        mAccountCreateSection = new View[] {findViewById(R.id.verification_code_prompt),
               findViewById(R.id.account_create_btn)};
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
     * If the phone number is valid, sends an account request to the server and changes the UI to
     * the verification view.
     *
     * @param v unused, only here so the function can be bound in the XML file
     */
    public void requestAccount(View v) {
        String raw_phone_number = mAreaEdit.getText().toString() + mLineEdit.getText().toString();
        Log.d(TAG, raw_phone_number);
        if (isValidPhoneForm(raw_phone_number)) {
            String phone_number = PhonebookUtils.convertToE164(raw_phone_number);
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
            String text = "The number should be in the form 'XXX XXX-XXXX'.";
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
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

    /**
     * Starts the RegistrationIntentService.
     *
     * @param v unused, only here so the function can be bound in the XML file
     */
    public void createNewAccount(View v) {
        startService(new Intent(this, RegistrationIntentService.class));
    }

    /**
     * Checks that the phone number is in a valid form.
     *
     * @param phone_number the phone number to check for validity
     * @return returns true if the phone number is a valid form
     */
    private boolean isValidPhoneForm(String phone_number) {
        String pattern = "\\d{6}-\\d{4}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(phone_number);
        return m.find();
    }

    private void setAreaEditListeners() {
        mAreaEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                // Restrict the length of the area code to 3 characters and send the overflow to
                // the line number
                if (s.length() > AREA_CODE_LENGTH) {
                    String overflow = s.subSequence(AREA_CODE_LENGTH, s.length()).toString();
                    s.delete(AREA_CODE_LENGTH, s.length());
                    mLineEdit.setText(overflow + mLineEdit.getText());
                    mLineEdit.requestFocus();
                    mLineEdit.setSelection(overflow.length());
                }
            }
        });
    }

    private void setLineEditListeners() {
        mLineEdit.addTextChangedListener(new TextWatcher() {
            // TODO: Revert first selection position of line number entry to last position of area
            //       code entry
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                // Restrict the length of the line number to the maximum of 8 (7 + 1 for the hyphen)
                if (s.length() > LINE_NUMBER_LENGTH+1) {
                    s.delete(LINE_NUMBER_LENGTH+1, s.length());
                }
                // Delete the hyphen character if it is in the wrong position or if it is the last
                // character in the line number
                for (int i = 0; i < s.length(); i++) {
                    if ((i != 3 || i == s.length()) && s.charAt(i) == '-') {
                        s.delete(i, i+1);
                    }
                }
                // Insert the hyphen character if the line number is at least four characters long
                if (s.length() >= 4) {
                    if (s.charAt(3) != '-') {
                        s.insert(3, "-");
                    }
                    // Delete the hyphen automatically if it's the last character so that the user
                    // doesn't have to do it themselves
                    if (s.charAt(s.length()-1) == '-') {
                        s.delete(s.length()-1, s.length());
                    }
                }

                if (mLineEdit.getSelectionStart() == mLineEdit.getSelectionEnd() &&
                        mLineEdit.getSelectionStart() == 0) {
                    mAreaEdit.requestFocus();
                    mAreaEdit.setSelection(mAreaEdit.length());
                }
            }
        });
    }

    /**
     * A receiver for handling token generation. This receiver will trigger the new account request
     * after generating the GCM token.
     */
    private class TokenBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
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

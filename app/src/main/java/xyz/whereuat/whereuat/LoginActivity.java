package xyz.whereuat.whereuat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.whereuat.whereuat.gcm.RegistrationIntentService;


public class LoginActivity extends AppCompatActivity {
    private EditText mPhoneEdit;
    private EditText mVerifyCode;
    private PreferenceController mPrefs;
    private RelativeLayout mAccountRequestSection;
    private RelativeLayout mAccountCreateSection;
    private HttpRequestHandler mHttpReqHandler;
    private TokenBroadcastReceiver mTokenReceiver;
    private IntentFilter mTokenFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mPrefs = new PreferenceController(this);
        mPhoneEdit = (EditText) findViewById(R.id.phone_number_input);
        mVerifyCode = (EditText) findViewById(R.id.verification_code_input);
        mAccountRequestSection = (RelativeLayout) findViewById(R.id.account_request_section);
        mAccountCreateSection = (RelativeLayout) findViewById(R.id.account_create_section);
        mHttpReqHandler = new HttpRequestHandler(this);

        mTokenFilter = new IntentFilter(Constants.TOKEN_BROADCAST);
        mTokenReceiver = new TokenBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mTokenReceiver, mTokenFilter);

        if (mPrefs.isWaitingForVerify())
            showCreateHideRequest();
        else
            showRequestHideCreate();
    }

    @Override
    protected void onPause() {
        try {
            unregisterReceiver(mTokenReceiver);
        } catch (IllegalArgumentException e) {}
        super.onPause();
    }

    @Override
    public void onResume() {
        try {
            registerReceiver(mTokenReceiver, mTokenFilter);
        } catch (IllegalArgumentException e) {}
        super.onResume();
    }

    private void showCreateHideRequest() {
        mAccountCreateSection.setVisibility(View.VISIBLE);
        mAccountRequestSection.setVisibility(View.GONE);
    }

    /*
        If the phone number is valid, sends an account request to the server and changes the UI to
        the verification view.
     */
    public void requestAccount(View v) {
        String phone_number = mPhoneEdit.getText().toString();
        if (isValidPhoneForm(phone_number)) {
            mHttpReqHandler.postAccountRequest(phone_number,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mPrefs.setWaitingForVerifyPref(true);
                            showCreateHideRequest();
                        }
                    },
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

    private void showRequestHideCreate() {
        mAccountCreateSection.setVisibility(View.GONE);
        mAccountRequestSection.setVisibility(View.VISIBLE);
    }

    public void createNewAccount(View v) {
        startService(new Intent(this, RegistrationIntentService.class));
    }

    /*
     * Checks that the phone number is in the valid form.
     */
    private boolean isValidPhoneForm(String phone_number) {
        String pattern = "\\+1\\d{10}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(phone_number);
        return m.find();
    }

    /*
        A receiver for handling token generation. This receiver will trigger the new account
        request after generating the GCM token.
     */
    private class TokenBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String token = intent.getStringExtra(Constants.TOKEN_EXTRA);
            mHttpReqHandler.postAccountNew(mPrefs.getClientPhoneNumber(), token,
                    mVerifyCode.getText().toString(),
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

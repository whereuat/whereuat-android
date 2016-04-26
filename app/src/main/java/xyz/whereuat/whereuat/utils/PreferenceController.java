package xyz.whereuat.whereuat.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages the SharedPreferences and provides a way to interact with them.
 */
public class PreferenceController {
    private static final String WHEREUAT_PREFS = "WHEREUAT_PREFS";
    private static final String HAS_ACCOUNT_PREF = "HAS_ACCOUNT";
    private static final String IS_WAITING_FOR_VERIFY_PREF = "IS_WAITING_FOR_VERIFY";
    private static final String CLIENT_PHONE_NUMBER_PREF = "CLIENT_PHONE_NUMBER";
    private SharedPreferences mPrefs;

    /**
     * Constructor for PreferenceController
     *
     * @param context Context of the application's shared preferences
     */
    public PreferenceController(Context context) {
        mPrefs = context.getSharedPreferences(WHEREUAT_PREFS, Context.MODE_PRIVATE);
    }

    /**
     * Retrieve whether or not the client has an account
     *
     * @return true if the client has an account
     */
    public boolean hasAccount() { return mPrefs.getBoolean(HAS_ACCOUNT_PREF, false); }

    /**
     * Retrieve whether or not the client is waiting for verification
     *
     * @return true if the client is waiting for verification
     */
    public boolean isWaitingForVerify() {
        return mPrefs.getBoolean(IS_WAITING_FOR_VERIFY_PREF, false);
    }

    /**
     * Retrieve the client's phone number
     *
     * @return String with the client's phone number, empty string if the phone number does not
     *         exist
     */
    public String getClientPhoneNumber() { return mPrefs.getString(CLIENT_PHONE_NUMBER_PREF, ""); }

    /**
     * Updates the phone number preference with the client's phone number entered at account
     * creation
     *
     * @param phone_number Client's entered phone number
     * @return true if the preference write was successful
     */
    public boolean setClientPhoneNumberPref(String phone_number) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(CLIENT_PHONE_NUMBER_PREF, phone_number);
        return editor.commit();
    }

    /**
     * Updates the preference for whether or not the user has a whereu@ account
     *
     * @param has_account Value to set the HAS_ACCOUNT_PREF to
     * @return true if successful write to the preference
     */
    public boolean setHasAccountPref(boolean has_account) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(HAS_ACCOUNT_PREF, has_account);
        return editor.commit();
    }

    /**
     * Updates the preference for whether or not the user is waiting to verify their account
     *
     * @param is_waiting_for_verify Value to set the IS_WAITING_FOR_VERIFY_PREF to
     * @return true if successful write to the preference
     */
    public boolean setWaitingForVerifyPref(boolean is_waiting_for_verify) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(IS_WAITING_FOR_VERIFY_PREF, is_waiting_for_verify);
        return editor.commit();
    }
}

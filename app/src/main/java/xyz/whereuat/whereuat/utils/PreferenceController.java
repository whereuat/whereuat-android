package xyz.whereuat.whereuat.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by julius on 3/25/16.
 *
 * This class manages the SharedPreferences and provides a way to interact with them.
 */
public class PreferenceController {
    private static final String WHEREUAT_PREFS = "WHEREUAT_PREFS";
    private static final String HAS_ACCOUNT_PREF = "HAS_ACCOUNT";
    private static final String IS_WAITING_FOR_VERIFY_PREF = "IS_WAITING_FOR_VERIFY";
    private static final String CLIENT_PHONE_NUMBER_PREF = "CLIENT_PHONE_NUMBER";
    private SharedPreferences mPrefs;

    public PreferenceController(Context context) {
        mPrefs = context.getSharedPreferences(WHEREUAT_PREFS, Context.MODE_PRIVATE);
    }

    public boolean hasAccount() { return mPrefs.getBoolean(HAS_ACCOUNT_PREF, false); }

    public boolean isWaitingForVerify() {
        return mPrefs.getBoolean(IS_WAITING_FOR_VERIFY_PREF, false);
    }

    /*
     * Updates the phone number preference with the client's phone number that they entered at
     * account creation. Returns true on a successful write to the preference.
     */
    public boolean setClientPhoneNumberPref(String phone_number) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(CLIENT_PHONE_NUMBER_PREF, phone_number);
        return editor.commit();
    }

    public String getClientPhoneNumber() { return mPrefs.getString(CLIENT_PHONE_NUMBER_PREF, ""); }

    /*
     * Updates the preference for whether or not the user has a whereu@ account. Returns true on a
     * successful write to the preference.
     */
    public boolean setHasAccountPref(boolean has_account) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(HAS_ACCOUNT_PREF, has_account);
        return editor.commit();
    }

    /*
     * Updates the preference for whether or not the user is waiting to verify their account.
     * Returns true on a successful write to the preference.
     */
    public boolean setWaitingForVerifyPref(boolean is_waiting_for_verify) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(IS_WAITING_FOR_VERIFY_PREF, is_waiting_for_verify);
        return editor.commit();
    }
}

package xyz.whereuat.whereuat;

/**
 * Created by julius on 4/3/16.
 *
 * This class is used to hold constants used throughout the application.
 */
public class Constants {
    // Constants related to the POST routes.
    public static final String WHEREUAT_URL = "http://whereuat.xyz/";
    public static final String ACCOUNT_REQUEST_ROUTE = "account/request";
    public static final String ACCOUNT_NEW_ROUTE = "account/new";
    public static final String AT_REQUEST_ROUTE = "where";
    public static final String AT_RESPONSE_ROUTE = "at";

    // Constants related to permissions.
    public static final int WHEREUAT_PERMISSION_REQUEST_LOCATION = 69;
    public static final int WHEREUAT_PERMISSION_REQUEST_READ_CONTACTS = 100;

    // Constants related to activity request codes
    public static final int PHONEBOOK_PICK_REQUEST = 200;

    // Constants related to extras in intents sent from the application.
    public static final String NOTIFICATION_ID_EXTRA = "NOTIFICATION_ID";
    public static final String TOKEN_EXTRA = "TOKEN";
    public static final String TO_PHONE_EXTRA = "TO_PHONE_EXTRA";

    public static final String CONTACT_NUMBER_BUNDLE_EXTRA = "CONTACT_NUMBER_BUNDLE_EXTRA";

    // Constants related to the broadcasts sent by the application.
    public static final String AT_RESPONSE_INITIATE_BROADCAST =
            "xyz.whereuat.whereuat.AT_RESPONSE_INITIATE_BROADCAST";
    public static final String TOKEN_BROADCAST = "xyz.whereuat.whereuat.TOKEN_BROADCAST";
    public static final String RELOAD_CONTACT_REQS_BROADCAST =
            "xyz.whereuat.whereuat.RELOAD_CONTACT_REQS_BROADCAST";
    public static final String RELOAD_CONTACTS_BROADCAST =
            "xyz.whereuat.whereuat.RELOAD_CONTACTS_BROADCAST";

    // The phone region to be used when validating phone numbers.
    public static final String DEFAULT_PHONE_REGION = "US";

    // The type of phone to be sent to the server when creating the user's account.
    public static final String CLIENT_OS = "ANDROID";

    // Constants for the keys in a notification.
    public static final String GCM_FROM_KEY = "from-#";
    public static final String GCM_PLACE_KEY = "place";

    // Constants used for the keys in a JSON POST request.
    public static final String JSON_CLIENT_OS_KEY = "client-os";
    public static final String JSON_CURR_LOC_KEY = "current-location";
    public static final String JSON_FROM_KEY = "from";
    public static final String JSON_GCM_TOK_KEY = "gcm-token";
    public static final String JSON_KEY_LOC_KEY = "key-location";
    public static final String JSON_LAT_KEY = "lat";
    public static final String JSON_LNG_KEY = "lng";
    public static final String JSON_PHONE_KEY = "phone-#";
    public static final String JSON_TO_KEY = "to";
    public static final String JSON_VERIFICATION_KEY = "verification-code";
}

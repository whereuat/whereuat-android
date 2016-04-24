package xyz.whereuat.whereuat;

/**
 * Created by julius on 4/3/16.
 */
public class Constants {
    public static final String WHEREUAT_URL = "http://whereuat.xyz/";
    public static final String ACCOUNT_REQUEST_ROUTE = "account/request";
    public static final String ACCOUNT_NEW_ROUTE = "account/new";
    public static final String AT_REQUEST_ROUTE = "where";
    public static final String AT_RESPONSE_ROUTE = "at";

    public static final int WHEREUAT_PERMISSION_REQUEST_LOCATION = 69;
    public static final int WHEREUAT_PERMISSION_REQUEST_READ_CONTACTS = 100;

    public static final String NOTIFICATION_ID_EXTRA = "NOTIFICATION_ID";
    public static final String TOKEN_EXTRA = "TOKEN";
    public static final String TO_PHONE_EXTRA = "TO_PHONE_EXTRA";

    public static final String AT_RESPONSE_INITIATE_BROADCAST =
            "xyz.whereuat.whereuat.AT_RESPONSE_INITIATE_BROADCAST";
    public static final String TOKEN_BROADCAST = "xyz.whereuat.whereuat.TOKEN_BROADCAST";

    public static final String DEFAULT_PHONE_REGION = "US";

    public static final String CLIENT_OS = "ANDROID";

    public static final String GCM_FROM_KEY = "from-#";
    public static final String GCM_PLACE_KEY = "place";

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

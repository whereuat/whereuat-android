package xyz.whereuat.whereuat;

/**
 * Created by julius on 4/3/16.
 */
public class Constants {
    public static final String WHEREUAT_URL = "http://whereuat.xyz/";
    public static final String ACCOUNT_REQUEST_ROUTE = "account/request";
    public static final String ACCOUNT_NEW_ROUTE = "account/new";

    public static final int WHEREUAT_PERMISSION_REQUEST_LOCATION = 69;
    public static final int WHEREUAT_PERMISSION_REQUEST_READ_CONTACTS = 100;

    public static final String CURR_LATITUDE_EXTRA = "CURR_LATITUDE_EXTRA";
    public static final String CURR_LONGITUDE_EXTRA = "CURR_LONGITUDE_EXTRA";
    public static final String RESPONSE_CODE_EXTRA = "RESPONSE_CODE";
    public static final String SHOULD_START_LOCATION_SERVICE = "SHOULD_START_LOCATION_SERVICE";
    public static final String TOKEN_EXTRA = "TOKEN";

    public static final String ACCOUNT_REQUEST_BROADCAST =
            "com.whereuat.whereu.ACCOUNT_REQUEST_BROADCAST";
    public static final String ACCOUNT_NEW_BROADCAST = "com.whereuat.whereu.ACCOUNT_NEW_BROADCAST";
    public static final String TOKEN_BROADCAST = "com.whereuat.whereu.TOKEN_BROADCAST";
    public static final String LOCATION_BROADCAST = "com.whereuat.whereu.LOCATION_BROADCAST";
}

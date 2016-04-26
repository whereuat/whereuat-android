package xyz.whereuat.whereuat.utils;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import xyz.whereuat.whereuat.Constants;

/**
 * <p>Utilities for HTTP requests with the server. Uses the
 * <a href="https://android.googlesource.com/platform/frameworks/volley">Volley</a> framework</p>
 */
public class HttpRequestHandler {
    private static RequestQueue mRequestQ;

    /**
     * Constructor for the HttpRequestHandler. Instantiates a request queue for Volley
     *
     * @param context Context for the Volley request queue
     */
    public HttpRequestHandler(Context context) {
        if (mRequestQ == null)
            mRequestQ = Volley.newRequestQueue(context);
    }

    /**
     * Method to build and initiate the POST request for requesting a new account from the server
     *
     * @param phone_number Phone number of the account
     * @param success_listener Listener for a success (200) response from the server
     * @param error_listener Listener for an error response from the server
     * @return true if POST request is built successfully
     */
    public boolean postAccountRequest(String phone_number,
                                      Response.Listener<String> success_listener,
                                      Response.ErrorListener error_listener) {
        JSONObject json = new JSONObject();
        try {
            json.put(Constants.JSON_PHONE_KEY, phone_number);
            post(Constants.ACCOUNT_REQUEST_ROUTE, json, success_listener, error_listener);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method to build and initiate the POST request for verifying a new account
     *
     * @param phone_number Phone number of the account
     * @param gcm_tok GCM token of the client device
     * @param verify_code Verification code for the account
     * @param success_listener Listener for a success (200) response from the server
     * @param error_listener Listener for an error response from the server
     * @return true if POST request is built successfully
     */
    public boolean postAccountNew(String phone_number, String gcm_tok, String verify_code,
                                  Response.Listener<String> success_listener,
                                  Response.ErrorListener error_listener) {
        JSONObject json = new JSONObject();
        try {
            json.put(Constants.JSON_PHONE_KEY, phone_number);
            json.put(Constants.JSON_GCM_TOK_KEY, gcm_tok);
            json.put(Constants.JSON_VERIFICATION_KEY, verify_code);
            json.put(Constants.JSON_CLIENT_OS_KEY, Constants.CLIENT_OS);
            post(Constants.ACCOUNT_NEW_ROUTE, json, success_listener, error_listener);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method to build and initiate the POST request for an @response
     *
     * @param from_phone Phone number of initiating client
     * @param to_phone Phone number of receiving client
     * @param lat Latitude of client's current location
     * @param lng Longitude of client's current location
     * @param key_loc Client's nearest key location
     * @param success_listener Listener for a success (200) response from the server
     * @param error_listener Listener for an error response from the server
     * @return true if POST request is built successfully
     */
    public boolean postAtResponse(String from_phone, String to_phone, double lat, double lng,
                                  KeyLocationUtils.KeyLocation key_loc,
                                  Response.Listener<String> success_listener,
                                  Response.ErrorListener error_listener) {
        JSONObject json = new JSONObject();
        try {
            json.put(Constants.JSON_FROM_KEY, from_phone);
            json.put(Constants.JSON_TO_KEY, to_phone);

            JSONObject curr_loc_json = new JSONObject();
            curr_loc_json.put(Constants.JSON_LAT_KEY, lat);
            curr_loc_json.put(Constants.JSON_LNG_KEY, lng);
            json.put(Constants.JSON_CURR_LOC_KEY, curr_loc_json);

            JSONObject key_loc_json = key_loc.toJson();
            json.put(Constants.JSON_KEY_LOC_KEY, key_loc_json == null ? JSONObject.NULL :
                                                                        key_loc_json);
            post(Constants.AT_RESPONSE_ROUTE, json, success_listener, error_listener);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method to build and initiate the POST request for an @request
     *
     * @param from_phone Phone number of initiating client
     * @param to_phone Phone number of receiving client
     * @param success_listener Listener for a success (200) response from the server
     * @param error_listener Listener for an error response from the server
     * @return true if POST request is built successfully
     */
    public boolean postAtRequest(String from_phone, String to_phone,
                                 Response.Listener<String> success_listener,
                                 Response.ErrorListener error_listener) {
        JSONObject json = new JSONObject();
        try {
            json.put(Constants.JSON_FROM_KEY, from_phone);
            json.put(Constants.JSON_TO_KEY, to_phone);

            post(Constants.AT_REQUEST_ROUTE, json, success_listener, error_listener);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Workhorse method for sending a POST request to the whereu@ server
     *
     * @param route Route on the server to direct the POST request to
     * @param json JSON body to attach to POST request
     * @param success_listener Listener for a success (200) response from the server
     * @param error_listener Listener for an error response from the server
     */
    private void post(String route, final JSONObject json,
                      Response.Listener<String> success_listener,
                      Response.ErrorListener error_listener) {
        StringRequest req = new StringRequest(Request.Method.POST, Constants.WHEREUAT_URL + route,
                success_listener, error_listener) {
            // We need to override these methods so we can send JSON and get a String response.
            @Override
            public byte[] getBody() throws AuthFailureError {
                return json.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        mRequestQ.add(req);
    }
}

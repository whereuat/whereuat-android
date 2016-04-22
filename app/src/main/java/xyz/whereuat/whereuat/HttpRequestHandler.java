package xyz.whereuat.whereuat;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by julius on 3/25/16.
 */
public class HttpRequestHandler {
    private static RequestQueue mRequestQ;

    public HttpRequestHandler(Context context) {
        if (mRequestQ == null)
            mRequestQ = Volley.newRequestQueue(context);
    }

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

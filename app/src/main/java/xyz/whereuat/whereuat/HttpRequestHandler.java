package xyz.whereuat.whereuat;

import android.content.Context;

import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
            json.put("phone-#", phone_number);
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
            json.put("phone-#", phone_number);
            json.put("gcm-token", gcm_tok);
            json.put("verification-code", verify_code);
            json.put("client-os", Constants.CLIENT_OS);
            post(Constants.ACCOUNT_NEW_ROUTE, json, success_listener, error_listener);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean postAtResponse(String from_phone, String to_phone, double lat, double lng,
                                  Response.Listener<String> success_listener,
                                  Response.ErrorListener error_listener) {
        JSONObject json = new JSONObject();
        try {
            json.put("from", from_phone);
            json.put("to", to_phone);

            JSONObject curr_loc_json = new JSONObject();
            curr_loc_json.put("lat", lat);
            curr_loc_json.put("lng", lng);
            json.put("current-location", curr_loc_json);

            // TODO: Tie this into the db.
            json.put("key-location", JSONObject.NULL);
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
            json.put("from", from_phone);
            json.put("to", to_phone);

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

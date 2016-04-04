package com.whereuat.whereu;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by julius on 3/25/16.
 */
public class HttpRequestHandler {
    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;

    private static final String ROUTE_EXTRA = "ROUTE";
    private static final String JSON_EXTRA = "JSON";
    private static final String BROADCAST_EXTRA = "BROADCAST";

    private Activity mActivity;

    public HttpRequestHandler(Activity calling_activity) {
        mActivity = calling_activity;
    }

    public boolean postAccountRequest(String phone_number) {
        JSONObject json = new JSONObject();
        try {
            json.put("phone-#", phone_number);
            post(Constants.ACCOUNT_REQUEST_ROUTE, json, Constants.ACCOUNT_REQUEST_BROADCAST);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean postAccountNew(String phone_number, String gcm_tok, String verify_code) {
        JSONObject json = new JSONObject();
        try {
            json.put("phone-#", phone_number);
            json.put("gcm-token", gcm_tok);
            json.put("verification-code", verify_code);
            post(Constants.ACCOUNT_NEW_ROUTE, json, Constants.ACCOUNT_NEW_BROADCAST);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void post(String route, JSONObject json, String broadcast) {
        Intent post = new Intent(mActivity, HttpPost.class);
        post.putExtra(ROUTE_EXTRA, route);
        post.putExtra(JSON_EXTRA, json.toString());
        post.putExtra(BROADCAST_EXTRA, broadcast);
        mActivity.startService(post);
    }

    /*
        A class for wrapping POSTs to the server in Intents
     */
    public static class HttpPost extends IntentService {
        public HttpPost() {
            super("HttpPost");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            String route = intent.getStringExtra(ROUTE_EXTRA);
            String json_str = intent.getStringExtra(JSON_EXTRA);
            String broadcast = intent.getStringExtra(BROADCAST_EXTRA);

            HttpURLConnection conn = null;
            try {
                URL url = new URL(Constants.WHEREUAT_URL + route);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(json_str);
                os.close();

                sendResultBroadcast(conn.getResponseCode(), broadcast);
            } catch (Exception e) {
                e.printStackTrace();
                sendResultBroadcast(-1, broadcast);
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
        }

        private void sendResultBroadcast(int response_code, String broadcast) {
            Intent result = new Intent();
            result.putExtra(Constants.RESPONSE_CODE_EXTRA, response_code);
            result.setAction(broadcast);
            sendBroadcast(result);
        }
    }
}

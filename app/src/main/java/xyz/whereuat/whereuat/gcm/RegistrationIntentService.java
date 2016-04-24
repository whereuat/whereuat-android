/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.whereuat.whereuat.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import xyz.whereuat.whereuat.Constants;
import xyz.whereuat.whereuat.R;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() { super(TAG); }

    @Override
    protected void onHandleIntent(Intent intent) {

        String token = "";
        try {
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local. R.string.gcm_defaultSenderId (the Sender ID) is typically derived from
            // google-services.json.
            InstanceID instanceID = InstanceID.getInstance(this);
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }
        // Broadcast the token to the login activity.
        Intent registrationComplete = new Intent(Constants.TOKEN_BROADCAST);
        registrationComplete.putExtra(Constants.TOKEN_EXTRA, token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}

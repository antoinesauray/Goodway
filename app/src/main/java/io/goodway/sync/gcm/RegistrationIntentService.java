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

package io.goodway.sync.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.ArrayList;

import io.goodway.R;
import io.goodway.model.Subscription;
import io.goodway.model.User;
import io.goodway.model.network.GoodwayHttpClientPost;
import io.goodway.navitia_android.Action;
import io.goodway.navitia_android.ErrorAction;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final GcmPubSub pubSub = GcmPubSub.getInstance(this);
        String userToken = intent.getStringExtra("token");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            final String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            Log.i(TAG, "GCM Registration Token: " + token);
            pubSub.subscribe(token,"/topics/global", null);

            // TODO: Implement this method to send any registration to your app's servers.
            GoodwayHttpClientPost.getSubscriptions(this, new Action<ArrayList<Subscription>>() {
                @Override
                public void action(ArrayList<Subscription> subscriptions) {
                    if (!subscriptions.isEmpty()) {
                        Log.d(TAG, "FOUND SUBSCRIPTIONS !");
                    }
                    for (final Subscription s : subscriptions) {
                            Log.d(TAG, "/topics/"+s.toString());
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    pubSub.subscribe(token,"/topics/"+s.getSchema()+"-"+s.getId_route()+"-"+s.getId_stop().replace(":", "_"), null);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }

                }
            }, new ErrorAction() {
                @Override
                public void action(int length) {
                    Log.d(TAG, "NO SUBSCRIPTIONS");
                }
            }, userToken);

            // Subscribe to topic subscriptions

            // [END get_token]

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }


        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        Log.d(TAG, token);
    }
    // [END subscribe_topics]
}

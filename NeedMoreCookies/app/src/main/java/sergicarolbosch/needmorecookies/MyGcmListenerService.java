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

package sergicarolbosch.needmorecookies;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * This class was extracted directly from the Google Cloud Messaging Github repository.
 * Extracted from: https://github.com/googlesamples/google-services/tree/master/android/gcm
 */

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    //Preferences
    private Boolean notifications;
    private String silent;
    private Boolean vibrate;

    NotificationCompat.Builder notificationBuilder;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String title = data.getString("title");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        /**[START Preferences]**/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Get currency User's preference
        notifications = prefs.getBoolean("notifications_new_message", true);
        silent = prefs.getString("notifications_new_message_ringtone", "content://settings/system/notification_sound");
        vibrate = prefs.getBoolean("notifications_new_message_vibrate", true);
        /**[END Preferences]**/

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(title, message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);


        //If notifications are enabled
        if (notifications) {
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.cookie_icon2_notifications)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            //If silence selected
            if (!silent.equals("")) {
                Uri defaultSoundUri = Uri.parse(silent);
                notificationBuilder.setSound(defaultSoundUri);
            }
            //If vibrate activated
            if (vibrate) {
                notificationBuilder.setVibrate(new long[]{500, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,500});
            }

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }
}

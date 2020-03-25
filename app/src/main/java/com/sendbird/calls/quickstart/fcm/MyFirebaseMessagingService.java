package com.sendbird.calls.quickstart.fcm;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.quickstart.utils.PrefUtils;
import com.sendbird.calls.quickstart.utils.PushUtils;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (SendBirdCall.handleFirebaseMessageData(remoteMessage.getData())) {
            Log.d(TAG, "[SendBirdCall Message] onMessageReceived() => " + remoteMessage.getData().toString());
        } else {
            Log.d(TAG, "onMessageReceived() => From: " + remoteMessage.getFrom());
            if (remoteMessage.getData().size() > 0) {
                Log.d(TAG, "onMessageReceived() => Data: " + remoteMessage.getData().toString());
            }
            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "onMessageReceived() => Notification Body: " + remoteMessage.getNotification().getBody());
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "onNewToken(token: " + token + ")");

        if (SendBirdCall.getCurrentUser() != null)  {
            PushUtils.registerPushToken(getApplicationContext(), token, e -> {
                if (e != null) {
                    Log.d(TAG, "registerPushTokenForCurrentUser() => e: " + e.getMessage());
                }
            });
        } else {
            PrefUtils.setPushToken(getApplicationContext(), token);
        }
    }
}

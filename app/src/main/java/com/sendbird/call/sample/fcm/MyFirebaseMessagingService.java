package com.sendbird.call.sample.fcm;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sendbird.call.SendBirdCall;
import com.sendbird.call.sample.BaseApplication;
import com.sendbird.call.sample.utils.PrefUtils;
import com.sendbird.call.sample.utils.PushUtils;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (SendBirdCall.handleFirebaseMessageData(remoteMessage.getData())) {
            Log.e(BaseApplication.TAG, "[MyFirebaseMessagingService][SendBirdCall Message] onMessageReceived() => " + remoteMessage.getData().toString());
        } else {
            Log.e(BaseApplication.TAG, "[MyFirebaseMessagingService] onMessageReceived() => From: " + remoteMessage.getFrom());
            if (remoteMessage.getData().size() > 0) {
                Log.e(BaseApplication.TAG, "[MyFirebaseMessagingService] onMessageReceived() => Data: " + remoteMessage.getData().toString());
            }
            if (remoteMessage.getNotification() != null) {
                Log.e(BaseApplication.TAG, "[MyFirebaseMessagingService] onMessageReceived() => Notification Body: " + remoteMessage.getNotification().getBody());
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.e(BaseApplication.TAG, "[MyFirebaseMessagingService] onNewToken(token: " + token + ")");

        if (SendBirdCall.getCurrentUser() != null)  {
            PushUtils.registerPushToken(getApplicationContext(), token, e -> {
                if (e != null) {
                    Log.e(BaseApplication.TAG, "[MyFirebaseMessagingService] registerPushTokenForCurrentUser() => e: " + e.getMessage());
                    return;
                }
            });
        } else {
            PrefUtils.setPushToken(getApplicationContext(), token);
        }
    }
}

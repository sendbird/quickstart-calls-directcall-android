package com.sendbird.calls.quickstart.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sendbird.calls.SendBirdCall.currentUser
import com.sendbird.calls.SendBirdCall.handleFirebaseMessageData
import com.sendbird.calls.quickstart.TAG
import com.sendbird.calls.quickstart.utils.registerPushToken
import com.sendbird.calls.quickstart.utils.setPushToken

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (handleFirebaseMessageData(remoteMessage.data)) {
            Log.i(TAG, "[MyFirebaseMessagingService] onMessageReceived() => " + remoteMessage.data.toString())
        }
    }

    override fun onNewToken(token: String) {
        Log.i(TAG, "[MyFirebaseMessagingService] onNewToken(token: $token)")
        if (currentUser != null) {
            registerPushToken(token) {
                if (it != null) {
                    Log.i(TAG,"[MyFirebaseMessagingService] registerPushTokenForCurrentUser() => e: " + it.message)
                }
            }
        } else {
            applicationContext.setPushToken(token)
        }
    }
}

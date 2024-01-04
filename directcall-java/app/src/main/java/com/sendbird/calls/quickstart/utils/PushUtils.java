package com.sendbird.calls.quickstart.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.SendBirdException;
import com.sendbird.calls.quickstart.BaseApplication;

public class PushUtils {

    public interface GetPushTokenHandler {
        void onResult(String token, SendBirdException e);
    }

    public static void getPushToken(Context context, final GetPushTokenHandler handler) {
        Log.i(BaseApplication.TAG, "[PushUtils] getPushToken()");

        String savedToken = PrefUtils.getPushToken(context);
        if (TextUtils.isEmpty(savedToken)) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.i(BaseApplication.TAG, "[PushUtils] getPushToken() => getInstanceId failed", task.getException());
                    if (handler != null) {
                        handler.onResult(null, new SendBirdException((task.getException() != null ? task.getException().getMessage() : "")));
                    }
                    return;
                }

                String pushToken = (task.getResult() != null ? task.getResult().getToken() : "");
                Log.i(BaseApplication.TAG, "[PushUtils] getPushToken() => pushToken: " + pushToken);
                if (handler != null) {
                    handler.onResult(pushToken, null);
                }
            });
        } else {
            Log.i(BaseApplication.TAG, "[PushUtils] savedToken: " + savedToken);
            if (handler != null) {
                handler.onResult(savedToken, null);
            }
        }
    }

    public interface PushTokenHandler {
        void onResult(SendBirdException e);
    }

    public static void registerPushToken(Context context, String pushToken, PushTokenHandler handler) {
        Log.i(BaseApplication.TAG, "[PushUtils] registerPushToken(pushToken: " + pushToken + ")");

        SendBirdCall.registerPushToken(pushToken, false, e -> {
            if (e != null) {
                Log.i(BaseApplication.TAG, "[PushUtils] registerPushToken() => e: " + e.getMessage());
                PrefUtils.setPushToken(context, pushToken);

                if (handler != null) {
                    handler.onResult(e);
                }
                return;
            }

            Log.i(BaseApplication.TAG, "[PushUtils] registerPushToken() => OK");
            PrefUtils.setPushToken(context, pushToken);

            if (handler != null) {
                handler.onResult(null);
            }
        });
    }
}

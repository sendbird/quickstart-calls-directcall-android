package com.sendbird.calls.quickstart;


import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.sendbird.calls.DirectCall;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.handler.SendBirdCallListener;
import com.sendbird.calls.quickstart.call.CallActivity;
import com.sendbird.calls.quickstart.utils.ActivityUtils;
import com.sendbird.calls.quickstart.utils.PrefUtils;

import java.util.UUID;

public class BaseApplication extends Application {

    public static final String VERSION = "1.0.1";

    private static final String TAG = "BaseApplication";

    // Refer to "https://github.com/sendbird/quickstart-calls-android".
    public static final String APP_ID = "YOUR_APPLICATION_ID";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        initSendBirdCall(PrefUtils.getAppId(getApplicationContext()));
    }

    public boolean initSendBirdCall(String appId) {
        Log.d(TAG, "initSendBirdCall(appId: " + appId + ")");
        Context context = getApplicationContext();

        if (TextUtils.isEmpty(appId)) {
            appId = APP_ID;
        }

        if (SendBirdCall.init(context, appId)) {
            SendBirdCall.removeAllListeners();
            SendBirdCall.addListener(UUID.randomUUID().toString(), new SendBirdCallListener() {
                @Override
                public void onRinging(DirectCall call) {
                    Log.d(TAG, "onRinging() => callId: " + call.getCallId());
                    if (CallActivity.sIsRunning) {
                        call.end();
                        return;
                    }
                    ActivityUtils.startCallActivityAsCallee(context, call);
                }
            });
            return true;
        }
        return false;
    }
}

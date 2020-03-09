package com.sendbird.call.sample;


import android.app.Application;
import android.util.Log;

import com.sendbird.call.DirectCall;
import com.sendbird.call.SendBirdCall;
import com.sendbird.call.handler.SendBirdCallListener;

import java.util.UUID;

public class BaseApplication extends Application {

    public static final String VERSION = "0.8.0";
    public static final String TAG = "SendBirdCall";

    // Refer to "https://github.com/sendbird/quickstart-calls-android".
    private static final String APP_ID = YOUR_APPLICATION_ID;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(BaseApplication.TAG, "[BaseApplication] onCreate()");

        SendBirdCall.init(getApplicationContext(), APP_ID);
        SendBirdCall.addListener(UUID.randomUUID().toString(), new SendBirdCallListener() {
            @Override
            public void onRinging(DirectCall call) {
                Log.e(BaseApplication.TAG, "[BaseApplication] onRinging() => callId: " + call.getCallId());
                CallActivity.startAsCallee(getApplicationContext(), call);
            }
        });
    }
}

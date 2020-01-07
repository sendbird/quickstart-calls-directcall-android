package com.sendbird.call.sample;


import android.app.Application;
import android.util.Log;

import com.sendbird.call.DirectCall;
import com.sendbird.call.SendBirdCall;
import com.sendbird.call.handler.SendBirdCallListener;

import java.util.UUID;

public class BaseApplication extends Application {

    public static final String VERSION = "0.6.0";
    public static final String TAG = "SendBirdCall";

    // Replace APP_ID with YOUR_APP_ID at dashboard(https://dashboard.sendbird.com).
    private static final String APP_ID = "31001FCA-5BE6-4A1F-A186-4A61D8C34505";

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

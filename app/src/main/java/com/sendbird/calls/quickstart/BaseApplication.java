package com.sendbird.calls.quickstart;


import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.sendbird.calls.DirectCall;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.handler.DirectCallListener;
import com.sendbird.calls.handler.SendBirdCallListener;
import com.sendbird.calls.quickstart.call.CallService;
import com.sendbird.calls.quickstart.utils.BroadcastUtils;
import com.sendbird.calls.quickstart.utils.PrefUtils;

import java.util.UUID;

public class BaseApplication extends Application {

    public static final String VERSION = "1.2.0";

    public static final String TAG = "SendBirdCalls";

    // Refer to "https://github.com/sendbird/quickstart-calls-android".
    public static final String APP_ID = "YOUR_APPLICATION_ID";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(BaseApplication.TAG, "[BaseApplication] onCreate()");

        initSendBirdCall(PrefUtils.getAppId(getApplicationContext()));
    }

    public boolean initSendBirdCall(String appId) {
        Log.i(BaseApplication.TAG, "[BaseApplication] initSendBirdCall(appId: " + appId + ")");
        Context context = getApplicationContext();

        if (TextUtils.isEmpty(appId)) {
            appId = APP_ID;
        }

        if (SendBirdCall.init(context, appId)) {
            SendBirdCall.removeAllListeners();
            SendBirdCall.addListener(UUID.randomUUID().toString(), new SendBirdCallListener() {
                @Override
                public void onRinging(DirectCall call) {
                    int ongoingCallCount = SendBirdCall.getOngoingCallCount();
                    Log.i(BaseApplication.TAG, "[BaseApplication] onRinging() => callId: " + call.getCallId() + ", getOngoingCallCount(): " + ongoingCallCount);

                    if (ongoingCallCount >= 2) {
                        call.end();
                        return;
                    }

                    call.setListener(new DirectCallListener() {
                        @Override
                        public void onConnected(DirectCall call) {
                        }

                        @Override
                        public void onEnded(DirectCall call) {
                            int ongoingCallCount = SendBirdCall.getOngoingCallCount();
                            Log.i(BaseApplication.TAG, "[BaseApplication] onEnded() => callId: " + call.getCallId() + ", getOngoingCallCount(): " + ongoingCallCount);

                            BroadcastUtils.sendCallLogBroadcast(context, call.getCallLog());

                            if (ongoingCallCount == 0) {
                                CallService.stopService(context);
                            }
                        }
                    });

                    CallService.onRinging(context, call);
                }
            });
            return true;
        }
        return false;
    }
}

package com.sendbird.calls.quickstart.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sendbird.calls.DirectCallLog;
import com.sendbird.calls.quickstart.BaseApplication;

public class BroadcastUtils {

    public static final String INTENT_ACTION_ADD_CALL_LOG = "com.sendbird.calls.quickstart.intent.action.ADD_CALL_LOG";
    public static final String INTENT_EXTRA_CALL_LOG = "call_log";

    public static void sendCallLogBroadcast(Context context, DirectCallLog callLog) {
        if (context != null && callLog != null) {
            Log.i(BaseApplication.TAG, "[BroadcastUtils] sendCallLogBroadcast()");

            Intent intent = new Intent(INTENT_ACTION_ADD_CALL_LOG);
            intent.putExtra(INTENT_EXTRA_CALL_LOG, callLog);
            context.sendBroadcast(intent);
        }
    }
}

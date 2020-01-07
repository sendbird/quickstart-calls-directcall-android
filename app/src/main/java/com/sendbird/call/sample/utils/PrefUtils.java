package com.sendbird.call.sample.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {

    private static final String PREF_NAME = "sendbird_calls";

    private static final String PREF_KEY_USER_ID    = "user_id";
    private static final String PREF_KEY_CALLEE_ID  = "callee_id";
    private static final String PREF_KEY_PUSH_TOKEN = "push_token";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    public static void setUserId(Context context, String userId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_KEY_USER_ID, userId).apply();
    }

    public static String getUserId(Context context) {
        return getSharedPreferences(context).getString(PREF_KEY_USER_ID, "");
    }

    public static void setCalleeId(Context context, String calleeId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_KEY_CALLEE_ID, calleeId).apply();
    }

    public static String getCalleeId(Context context) {
        return getSharedPreferences(context).getString(PREF_KEY_CALLEE_ID, "");
    }

    public static void setPushToken(Context context, String pushToken) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_KEY_PUSH_TOKEN, pushToken).apply();
    }

    public static String getPushToken(Context context) {
        return getSharedPreferences(context).getString(PREF_KEY_PUSH_TOKEN, "");
    }
}

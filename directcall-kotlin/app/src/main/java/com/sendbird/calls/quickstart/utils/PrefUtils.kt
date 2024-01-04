package com.sendbird.calls.quickstart.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import com.sendbird.calls.quickstart.APP_ID

private const val PREF_NAME = "sendbird_calls"
private const val PREF_KEY_APP_ID = "app_id"
private const val PREF_KEY_USER_ID = "user_id"
private const val PREF_KEY_ACCESS_TOKEN = "access_token"
private const val PREF_KEY_CALLEE_ID = "callee_id"
private const val PREF_KEY_PUSH_TOKEN = "push_token"

private fun Context.getSharedPreferences(): SharedPreferences {
    return getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}

fun Context.setAppId(appId: String?) {
    val editor = getSharedPreferences().edit()
    editor.putString(PREF_KEY_APP_ID, appId).apply()
}

fun Context.getAppId(): String? {
    return this.getSharedPreferences().getString(PREF_KEY_APP_ID, APP_ID)
}

fun Context.setUserId(userId: String?) {
    val editor = getSharedPreferences().edit()
    editor.putString(PREF_KEY_USER_ID, userId).apply()
}

fun Context.getUserId(): String? {
    return getSharedPreferences().getString(PREF_KEY_USER_ID, "")
}

fun Context.setAccessToken(accessToken: String?) {
    val editor = getSharedPreferences().edit()
    editor.putString(PREF_KEY_ACCESS_TOKEN, accessToken).apply()
}

fun Context.getAccessToken(): String? {
    return getSharedPreferences().getString(PREF_KEY_ACCESS_TOKEN, "")
}

fun Context.setCalleeId(calleeId: String?) {
    val editor = getSharedPreferences().edit()
    editor.putString(PREF_KEY_CALLEE_ID, calleeId).apply()
}

fun Fragment.setCalleeId(calleeId: String?) {
    requireContext().setCalleeId(calleeId)
}

fun Context.getCalleeId(): String? {
    return getSharedPreferences().getString(PREF_KEY_CALLEE_ID, "")
}

fun Fragment.getCalleeId(): String? {
    return requireContext().getCalleeId()
}

fun Context.setPushToken(pushToken: String?) {
    val editor = getSharedPreferences().edit()
    editor.putString(PREF_KEY_PUSH_TOKEN, pushToken).apply()
}

fun Context.getPushToken(): String? {
    return getSharedPreferences().getString(PREF_KEY_PUSH_TOKEN, "")
}

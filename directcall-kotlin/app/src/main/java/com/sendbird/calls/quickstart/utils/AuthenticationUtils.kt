package com.sendbird.calls.quickstart.utils

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.util.Log
import com.sendbird.calls.AuthenticateParams
import com.sendbird.calls.SendBirdCall.applicationId
import com.sendbird.calls.SendBirdCall.authenticate
import com.sendbird.calls.SendBirdCall.currentUser
import com.sendbird.calls.SendBirdCall.deauthenticate
import com.sendbird.calls.SendBirdCall.registerPushToken
import com.sendbird.calls.SendBirdCall.unregisterPushToken
import com.sendbird.calls.SendBirdException
import com.sendbird.calls.handler.CompletionHandler
import com.sendbird.calls.quickstart.BaseApplication
import com.sendbird.calls.quickstart.R
import com.sendbird.calls.quickstart.TAG
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

object AuthenticationUtils {
    fun authenticate(
        context: Context,
        userId: String?,
        accessToken: String?,
        handler: AuthenticateHandler?
    ) {
        if (userId == null) {
            Log.i(TAG, "[AuthenticationUtils] authenticate() => Failed (userId == null)")
            handler?.onResult(false)
            return
        }
        deauthenticate(context) {
            context.getPushToken { token, e ->
                if (e != null) {
                    Log.i(TAG,"[AuthenticationUtils] authenticate() => Failed (e: " + e.message + ")")
                    handler?.onResult(false)
                    return@getPushToken
                }
                Log.i(TAG, "[AuthenticationUtils] authenticate(userId: $userId)")
                authenticate(AuthenticateParams(userId).setAccessToken(accessToken)) { _, e1 ->
                    if (e1 != null || token == null) {
                        Log.i(TAG,"[AuthenticationUtils] authenticate() => Failed (e1: " + e1?.message + ")")
                        context.showToastErrorMessage(e1)
                        handler?.onResult(false)
                        return@authenticate
                    }
                    Log.i(TAG,"[AuthenticationUtils] authenticate() => registerPushToken(pushToken: $token)")
                    registerPushToken(token,false) { e2 ->
                        if (e2 != null) {
                            Log.i(TAG,"[AuthenticationUtils] authenticate() => registerPushToken() => Failed (e2: " + e2.message + ")")
                            context.showToastErrorMessage(e2)
                            handler?.onResult(false)
                            return@registerPushToken
                        }
                        context.setAppId(applicationId)
                        context.setUserId(userId)
                        context.setAccessToken(accessToken)
                        context.setPushToken(token)
                        Log.i(TAG, "[AuthenticationUtils] authenticate() => OK")
                        handler?.onResult(true)
                    }
                }
            }
        }
    }

    fun authenticateWithEncodedAuthInfo(
        activity: Activity,
        encodedAuthInfo: String?,
        handler: CompletionWithDetailHandler?
    ) {
        var appId: String? = null
        var userId: String? = null
        var accessToken: String? = null
        try {
            if (!encodedAuthInfo.isNullOrEmpty()) {
                val jsonString = String(Base64.decode(encodedAuthInfo, Base64.DEFAULT))
                val jsonObject = JSONObject(jsonString)
                appId = jsonObject.getString("app_id")
                userId = jsonObject.getString("user_id")
                accessToken = jsonObject.getString("access_token")
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        if (!appId.isNullOrEmpty() && !userId.isNullOrEmpty() && (activity.application as BaseApplication).initSendBirdCall(appId)) {
            authenticate(activity, userId,accessToken) { isSuccess -> handler?.onCompletion(isSuccess, false) }
        } else {
            handler?.onCompletion(isSuccess = false, hasInvalidValue = true)
        }
    }

    fun deauthenticate(context: Context, handler: DeauthenticateHandler?) {
        if (currentUser == null) {
            handler?.onResult(false)
            return
        }
        Log.i(TAG,"[AuthenticationUtils] deauthenticate(userId: " + currentUser?.userId + ")")
        val pushToken = context.getPushToken()
        if (!pushToken.isNullOrEmpty()) {
            Log.i(TAG,"[AuthenticationUtils] deauthenticate() => unregisterPushToken(pushToken: $pushToken)")
            unregisterPushToken(pushToken, CompletionHandler { e: SendBirdException? ->
                if (e != null) {
                    Log.i(TAG,"[AuthenticationUtils] unregisterPushToken() => Failed (e: " + e.message + ")")
                    context.showToastErrorMessage(e)
                }
                doDeauthenticate(context, handler)
            })
        } else {
            doDeauthenticate(context, handler)
        }
    }

    private fun doDeauthenticate(context: Context, handler: DeauthenticateHandler?) {
        deauthenticate { e: SendBirdException? ->
            if (e != null) {
                Log.i(
                    TAG,
                    "[AuthenticationUtils] deauthenticate() => Failed (e: " + e.message + ")"
                )
                context.showToastErrorMessage(e)
            } else {
                Log.i(TAG, "[AuthenticationUtils] deauthenticate() => OK")
            }
            context.setUserId(null)
            context.setAccessToken(null)
            context.setCalleeId(null)
            context.setPushToken(null)
            handler?.onResult(e == null)
        }
    }

    fun autoAuthenticate(context: Context?, handler: AutoAuthenticateHandler?) {
        Log.i(TAG, "[AuthenticationUtils] autoAuthenticate()")
        if (currentUser != null) {
            Log.i(TAG,"[AuthenticationUtils] autoAuthenticate(userId: " + currentUser?.userId + ") => OK (SendBirdCall.getCurrentUser() != null)")
            handler?.onResult(currentUser?.userId)
            return
        }
        val userId = context?.getUserId()
        val accessToken = context?.getAccessToken()
        val pushToken = context?.getPushToken()
        if (!userId.isNullOrEmpty() && !pushToken.isNullOrEmpty()) {
            Log.i(TAG,"[AuthenticationUtils] autoAuthenticate() => authenticate(userId: $userId)")
            authenticate(AuthenticateParams(userId).setAccessToken(accessToken)) { _, e ->
                if (e != null) {
                    Log.i(TAG,"[AuthenticationUtils] autoAuthenticate() => authenticate() => Failed (e: " + e.message + ")")
                    context.showToastErrorMessage(e)
                    handler?.onResult(null)
                    return@authenticate
                }
                Log.i(TAG,"[AuthenticationUtils] autoAuthenticate() => registerPushToken(pushToken: $pushToken)")
                registerPushToken(
                    pushToken,
                    false,
                    CompletionHandler { e1: SendBirdException? ->
                        if (e1 != null) {
                            Log.i(TAG,"[AuthenticationUtils] autoAuthenticate() => registerPushToken() => Failed (e1: " + e1.message + ")")
                            context.showToastErrorMessage(e1)
                            handler?.onResult(null)
                            return@CompletionHandler
                        }
                        Log.i(TAG,"[AuthenticationUtils] autoAuthenticate() => authenticate() => OK (Authenticated)")
                        handler?.onResult(userId)
                    })
            }
        } else {
            Log.i(TAG,"[AuthenticationUtils] autoAuthenticate() => Failed (No userId and pushToken)")
            handler?.onResult(null)
        }
    }
}

fun interface AuthenticateHandler {
    fun onResult(isSuccess: Boolean)
}

fun interface CompletionWithDetailHandler {
    fun onCompletion(isSuccess: Boolean, hasInvalidValue: Boolean)
}

fun interface DeauthenticateHandler {
    fun onResult(isSuccess: Boolean)
}

fun interface AutoAuthenticateHandler {
    fun onResult(userId: String?)
}

fun Context.showToastErrorMessage(e: SendBirdException?) {
    if (e?.code == 400111) {
        showToast(getString(R.string.calls_invalid_notifications_setting_in_dashboard))
    } else {
        showToast(e?.message ?: "")
    }
}
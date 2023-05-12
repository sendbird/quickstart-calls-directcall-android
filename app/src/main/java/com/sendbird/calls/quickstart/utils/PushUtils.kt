package com.sendbird.calls.quickstart.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.sendbird.calls.SendBirdCall.registerPushToken
import com.sendbird.calls.SendBirdException
import com.sendbird.calls.handler.CompletionHandler
import com.sendbird.calls.quickstart.TAG

fun Context.getPushToken(handler: GetPushTokenHandler?) {
    Log.i(TAG, "[PushUtils] getPushToken()")
    val savedToken = getPushToken()
    if (savedToken.isNullOrEmpty()) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(
            OnCompleteListener<InstanceIdResult?> { task: Task<InstanceIdResult?> ->
                val pushToken =  task.result?.token
                if (!task.isSuccessful || pushToken == null) {
                    Log.i(TAG, "[PushUtils] getPushToken() => getInstanceId failed", task.exception)
                    handler?.onResult(null, SendBirdException(if (task.exception != null) task.exception?.message else ""))
                    return@OnCompleteListener
                }
                Log.i(TAG,"[PushUtils] getPushToken() => pushToken: $pushToken")
                handler?.onResult(pushToken, null)
            })
    } else {
        Log.i(TAG, "[PushUtils] savedToken: $savedToken")
        handler?.onResult(savedToken, null)
    }
}

fun Context.registerPushToken(pushToken: String, handler: PushTokenHandler?) {
    Log.i(TAG, "[PushUtils] registerPushToken(pushToken: $pushToken)")
    registerPushToken(pushToken, false, CompletionHandler { e: SendBirdException? ->
        if (e != null) {
            Log.i(TAG, "[PushUtils] registerPushToken() => e: " + e.message)
            setPushToken(pushToken)
            handler?.onResult(e)
            return@CompletionHandler
        }
        Log.i(TAG, "[PushUtils] registerPushToken() => OK")
        setPushToken(pushToken)
        handler?.onResult(null)
    })
}

fun interface GetPushTokenHandler {
    fun onResult(token: String?, e: SendBirdException?)
}

fun interface PushTokenHandler {
    fun onResult(e: SendBirdException?)
}
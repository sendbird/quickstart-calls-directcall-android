package com.sendbird.calls.quickstart

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.sendbird.calls.DirectCall
import com.sendbird.calls.RoomInvitation
import com.sendbird.calls.SendBirdCall.Options.addDirectCallSound
import com.sendbird.calls.SendBirdCall.SoundType
import com.sendbird.calls.SendBirdCall.addListener
import com.sendbird.calls.SendBirdCall.init
import com.sendbird.calls.SendBirdCall.ongoingCallCount
import com.sendbird.calls.SendBirdCall.removeAllListeners
import com.sendbird.calls.handler.DirectCallListener
import com.sendbird.calls.handler.SendBirdCallListener
import com.sendbird.calls.quickstart.call.CallService
import com.sendbird.calls.quickstart.utils.getAppId
import com.sendbird.calls.quickstart.utils.sendCallLogBroadcast
import java.util.*

// multidex
const val VERSION = "1.4.0"
const val TAG = "SendBirdCalls"

// Refer to "https://github.com/sendbird/quickstart-calls-android".
const val APP_ID = "YOUR_APPLICATION_ID"

class BaseApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "[BaseApplication] onCreate()")
        initSendBirdCall(getAppId())
    }

    fun initSendBirdCall(appId: String?): Boolean {
        Log.i(TAG, "[BaseApplication] initSendBirdCall(appId: $appId)")
        val applicationId = if (appId.isNullOrBlank()) APP_ID else appId
        if (init(applicationContext, applicationId)) {
            removeAllListeners()
            addListener(UUID.randomUUID().toString(), object : SendBirdCallListener() {
                override fun onInvitationReceived(invitation: RoomInvitation) {}

                override fun onRinging(call: DirectCall) {
                    val ongoingCallCount = ongoingCallCount
                    Log.i(TAG,"[BaseApplication] onRinging() => callId: " + call.callId + ", getOngoingCallCount(): " + ongoingCallCount)
                    if (ongoingCallCount >= 2) {
                        call.end()
                        return
                    }
                    call.setListener(object : DirectCallListener() {
                        override fun onConnected(call: DirectCall) {}
                        override fun onEnded(call: DirectCall) {
                            Log.i(TAG,"[BaseApplication] onEnded() => callId: " + call.callId + ", getOngoingCallCount(): " + ongoingCallCount)
                            sendCallLogBroadcast(call.callLog)
                            if (ongoingCallCount == 0) {
                                CallService.stopService(applicationContext)
                            }
                        }
                    })
                    CallService.onRinging(applicationContext, call)
                }
            })
            addDirectCallSound(SoundType.DIALING, R.raw.dialing)
            addDirectCallSound(SoundType.RINGING, R.raw.ringing)
            addDirectCallSound(SoundType.RECONNECTING, R.raw.reconnecting)
            addDirectCallSound(SoundType.RECONNECTED, R.raw.reconnected)
            return true
        }
        return false
    }
}
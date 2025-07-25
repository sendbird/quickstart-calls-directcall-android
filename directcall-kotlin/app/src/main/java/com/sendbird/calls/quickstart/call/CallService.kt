package com.sendbird.calls.quickstart.call

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sendbird.calls.DirectCall
import com.sendbird.calls.SendBirdCall.ongoingCallCount
import com.sendbird.calls.quickstart.R
import com.sendbird.calls.quickstart.TAG
import com.sendbird.calls.quickstart.main.FullscreenNotificationActivity.Companion.getFullScreenNotificationActivityIntent
import com.sendbird.calls.quickstart.utils.getNicknameOrUserId
import com.sendbird.calls.quickstart.utils.showToast

private const val NOTIFICATION_ID = 1
const val EXTRA_IS_HEADS_UP_NOTIFICATION = "is_heads_up_notification"
const val EXTRA_REMOTE_NICKNAME_OR_USER_ID = "remote_nickname_or_user_id"
const val EXTRA_CALL_STATE = "call_state"
const val EXTRA_CALL_ID = "call_id"
const val EXTRA_IS_VIDEO_CALL = "is_video_call"
const val EXTRA_CALLEE_ID_TO_DIAL = "callee_id_to_dial"
const val EXTRA_DO_DIAL = "do_dial"
const val EXTRA_DO_ACCEPT = "do_accept"
const val EXTRA_DO_LOCAL_VIDEO_START = "do_local_video_start"
const val EXTRA_DO_END = "do_end"

class CallService : Service() {
    private val mBinder: IBinder = CallBinder()
    private val mServiceData = ServiceData()

    internal inner class CallBinder : Binder() {
        val service: CallService
            get() = this@CallService
    }

    override fun onBind(intent: Intent): IBinder {
        Log.i(TAG, "[CallService] onBind()")
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "[CallService] onCreate()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "[CallService] onDestroy()")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "[CallService] onStartCommand()")
        mServiceData.isHeadsUpNotification = intent.getBooleanExtra(EXTRA_IS_HEADS_UP_NOTIFICATION, false)
        mServiceData.remoteNicknameOrUserId = intent.getStringExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID)
        mServiceData.callState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_CALL_STATE, STATE::class.java)
        } else {
            intent.getSerializableExtra(EXTRA_CALL_STATE) as STATE
        }
        mServiceData.callId = intent.getStringExtra(EXTRA_CALL_ID)
        mServiceData.isVideoCall = intent.getBooleanExtra(EXTRA_IS_VIDEO_CALL, false)
        mServiceData.calleeIdToDial = intent.getStringExtra(EXTRA_CALLEE_ID_TO_DIAL)
        mServiceData.doDial = intent.getBooleanExtra(EXTRA_DO_DIAL, false)
        mServiceData.doAccept = intent.getBooleanExtra(EXTRA_DO_ACCEPT, false)
        mServiceData.doLocalVideoStart = intent.getBooleanExtra(EXTRA_DO_LOCAL_VIDEO_START, false)
        updateNotification(mServiceData)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        Log.i(TAG, "[CallService] onTaskRemoved()")
        mServiceData.isHeadsUpNotification = true
        updateNotification(mServiceData)
    }

    private fun getNotification(serviceData: ServiceData): Notification {
        val content: String = if (serviceData.isVideoCall) {
            getString(
                R.string.calls_notification_video_calling_content,
                getString(R.string.calls_app_name)
            )
        } else {
            getString(
                R.string.calls_notification_voice_calling_content,
                getString(R.string.calls_app_name)
            )
        }
        val currentTime = System.currentTimeMillis().toInt()
        val channelId = packageName + currentTime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.calls_app_name)
            val channel = NotificationChannel(channelId, channelName, if (serviceData.isHeadsUpNotification) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val pendingIntentFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val callIntent = getCallActivityIntent(this, serviceData, false)
        val callPendingIntent = PendingIntent.getActivity(this, currentTime + 1, callIntent, pendingIntentFlag)
        val endIntent = getCallActivityIntent(this, serviceData, true)
        val endPendingIntent = PendingIntent.getActivity(this, currentTime + 2, endIntent, pendingIntentFlag)
        val fullScreenIntent = getFullScreenNotificationActivityIntent(applicationContext, serviceData)
        val fullScreenPendingIntent = PendingIntent.getActivity(this, currentTime + 2, fullScreenIntent, pendingIntentFlag)
        val builder = NotificationCompat.Builder(this, channelId).apply {
            setContentTitle(serviceData.remoteNicknameOrUserId)
            setContentText(content)
            setSmallIcon(R.drawable.ic_sendbird)
            setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.icon_push_oreo))
            priority = NotificationCompat.PRIORITY_HIGH
            setCategory(NotificationCompat.CATEGORY_CALL)
            if (ongoingCallCount > 0) {
                if (serviceData.doAccept) {
                    addAction(NotificationCompat.Action(0,getString(R.string.calls_notification_decline),endPendingIntent))
                    addAction(NotificationCompat.Action(  0, getString(R.string.calls_notification_accept), callPendingIntent))

                    // Use a full-screen intent only for the highest-priority alerts where you
                    // have an associated activity that you would like to launch after the user
                    // interacts with the notification. Also, if your app targets Android 10
                    // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
                    // order for the platform to invoke this notification.
                    // The system UI may choose to display a heads-up notification, instead of launching this intent, while the user is using the device.
                    // https://developer.android.com/reference/android/app/Notification.Builder#setFullScreenIntent(android.app.PendingIntent,%20boolean)
                    setFullScreenIntent(fullScreenPendingIntent, true)
                } else {
                    setContentIntent(callPendingIntent)
                    addAction(NotificationCompat.Action(0, getString(R.string.calls_notification_end), endPendingIntent))
                }
            }
        }
        return builder.build()
    }

    fun updateNotification(serviceData: ServiceData) {
        Log.i(
            TAG,
            "[CallService] updateNotification(isHeadsUpNotification: " + serviceData.isHeadsUpNotification + ", remoteNicknameOrUserId: " + serviceData.remoteNicknameOrUserId
                    + ", callState: " + serviceData.callState + ", callId: " + serviceData.callId + ", isVideoCall: " + serviceData.isVideoCall
                    + ", calleeIdToDial: " + serviceData.calleeIdToDial + ", doDial: " + serviceData.doDial + ", doAccept: " + serviceData.doAccept + ", doLocalVideoStart: " + serviceData.doLocalVideoStart + ")"
        )
        mServiceData.set(serviceData)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                getNotification(mServiceData),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(NOTIFICATION_ID, getNotification(mServiceData))
        }
    }

    companion object {
        private fun getCallActivityIntent(context: Context, serviceData: ServiceData, doEnd: Boolean) =
            if (serviceData.isVideoCall) {
                Intent(context, VideoCallActivity::class.java)
            } else {
                Intent(context, VoiceCallActivity::class.java)
            }.apply {
                putExtra(EXTRA_CALL_STATE, serviceData.callState)
                putExtra(EXTRA_CALL_ID, serviceData.callId)
                putExtra(EXTRA_IS_VIDEO_CALL, serviceData.isVideoCall)
                putExtra(EXTRA_CALLEE_ID_TO_DIAL, serviceData.calleeIdToDial)
                putExtra(EXTRA_DO_DIAL, serviceData.doDial)
                putExtra(EXTRA_DO_ACCEPT, serviceData.doAccept)
                putExtra(EXTRA_DO_LOCAL_VIDEO_START, serviceData.doLocalVideoStart)
                putExtra(EXTRA_DO_END, doEnd)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }

        fun dial(context: Context, doDialWithCalleeId: String, isVideoCall: Boolean) {
            if (ongoingCallCount > 0) {
                context.showToast("Ringing.")
                Log.i(TAG,"[CallService] dial() => SendBirdCall.getOngoingCallCount(): $ongoingCallCount")
                return
            }
            Log.i(TAG, "[CallService] dial()")
            val serviceData = ServiceData().apply {
                isHeadsUpNotification = false
                remoteNicknameOrUserId = doDialWithCalleeId
                callState = STATE.STATE_OUTGOING
                callId = null
                this.isVideoCall = isVideoCall
                calleeIdToDial = doDialWithCalleeId
                doDial = true
                doAccept = false
                doLocalVideoStart = false
            }
            startService(context, serviceData)
            context.startActivity(getCallActivityIntent(context, serviceData, false))
        }

        fun onRinging(context: Context, call: DirectCall) {
            Log.i(TAG, "[CallService] onRinging()")
            val serviceData = ServiceData().apply {
                isHeadsUpNotification = true
                remoteNicknameOrUserId = call.remoteUser.getNicknameOrUserId()
                callState = STATE.STATE_ACCEPTING
                callId = call.callId
                isVideoCall = call.isVideoCall
                calleeIdToDial = null
                doDial = false
                doAccept = true
                doLocalVideoStart = false
            }
            startService(context, serviceData)
        }

        private fun startService(context: Context, serviceData: ServiceData) {
            Log.i(TAG, "[CallService] startService()")
            val intent = Intent(context, CallService::class.java).apply {
                putExtra(EXTRA_IS_HEADS_UP_NOTIFICATION, serviceData.isHeadsUpNotification)
                putExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID, serviceData.remoteNicknameOrUserId)
                putExtra(EXTRA_CALL_STATE, serviceData.callState)
                putExtra(EXTRA_CALL_ID, serviceData.callId)
                putExtra(EXTRA_IS_VIDEO_CALL, serviceData.isVideoCall)
                putExtra(EXTRA_CALLEE_ID_TO_DIAL, serviceData.calleeIdToDial)
                putExtra(EXTRA_DO_DIAL, serviceData.doDial)
                putExtra(EXTRA_DO_ACCEPT, serviceData.doAccept)
                putExtra(EXTRA_DO_LOCAL_VIDEO_START, serviceData.doLocalVideoStart)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            Log.i(TAG, "[CallService] stopService()")
            val intent = Intent(context, CallService::class.java)
            context.stopService(intent)
        }
    }
}

data class ServiceData(
    var isHeadsUpNotification: Boolean = false,
    var remoteNicknameOrUserId: String? = null,
    var callState: STATE? = null,
    var callId: String? = null,
    var isVideoCall: Boolean = false,
    var calleeIdToDial: String? = null,
    var doDial: Boolean = false,
    var doAccept: Boolean = false,
    var doLocalVideoStart: Boolean = false,
) {
    fun set(serviceData: ServiceData) {
        isHeadsUpNotification = serviceData.isHeadsUpNotification
        remoteNicknameOrUserId = serviceData.remoteNicknameOrUserId
        callState = serviceData.callState
        callId = serviceData.callId
        isVideoCall = serviceData.isVideoCall
        calleeIdToDial = serviceData.calleeIdToDial
        doDial = serviceData.doDial
        doAccept = serviceData.doAccept
        doLocalVideoStart = serviceData.doLocalVideoStart
    }
}
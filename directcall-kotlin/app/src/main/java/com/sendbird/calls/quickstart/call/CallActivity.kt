package com.sendbird.calls.quickstart.call

import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.sendbird.calls.AudioDevice
import com.sendbird.calls.ConnectionQualityMonitoringMode
import com.sendbird.calls.ConnectionQualityState
import com.sendbird.calls.DirectCall
import com.sendbird.calls.SendBirdCall.currentUser
import com.sendbird.calls.SendBirdCall.getCall
import com.sendbird.calls.handler.DirectCallListener
import com.sendbird.calls.quickstart.R
import com.sendbird.calls.quickstart.TAG
import com.sendbird.calls.quickstart.call.CallService.CallBinder
import com.sendbird.calls.quickstart.utils.AuthenticationUtils.autoAuthenticate
import com.sendbird.calls.quickstart.utils.getEndResultString
import com.sendbird.calls.quickstart.utils.getNicknameOrUserId
import com.sendbird.calls.quickstart.utils.sendCallLogBroadcast
import com.sendbird.calls.quickstart.utils.setProfileImage
import java.util.*

const val ENDING_TIME_MS = 1000

abstract class CallActivity : AppCompatActivity() {
    abstract val binding: ViewBinding

    private var mCallService: CallService? = null
    private var mBound = false
    private var mCallId: String? = null
    private var mDoDial = false
    private var mDoAccept = false
    private var mDoEnd = false
    private var mEndingTimer: Timer? = null

    protected var mState: STATE? = null
    protected var mIsVideoCall = false
    protected var mCalleeIdToDial: String? = null
    protected var mDoLocalVideoStart = false

    protected var mDirectCall: DirectCall? = null
    protected var mIsAudioEnabled = false

    // region Views
    abstract val mLinearLayoutInfo: LinearLayout
    abstract val mImageViewProfile: ImageView
    abstract val mTextViewUserId: TextView
    abstract val mTextViewStatus: TextView
    abstract val mLinearLayoutRemoteMute: LinearLayout
    abstract val mTextViewRemoteMute: TextView
    abstract val mRelativeLayoutRingingButtons: RelativeLayout
    abstract val mImageViewDecline: ImageView
    abstract val mLinearLayoutConnectingButtons: LinearLayout
    abstract val mImageViewAudioOff: ImageView
    abstract val mImageViewBluetooth: ImageView
    abstract val mImageViewEnd: ImageView
    abstract val mConstraintLayoutNetworkIndicator: ConstraintLayout
    abstract val mImageViewNetworkIndicator: ImageView
    abstract val mTextViewNetworkIndicator: TextView
    // endregion

    protected abstract fun setAudioDevice(currentAudioDevice: AudioDevice?, availableAudioDevices: Set<AudioDevice>?)
    protected abstract fun startCall(amICallee: Boolean)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "[CallActivity] onCreate()")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        setContentView(binding.root)
        bindCallService()
        init()
        setViews()
        setAudioDevice()
        setCurrentState()
        if (mDoEnd) {
            Log.i(TAG, "[CallActivity] init() => (mDoEnd == true)")
            end()
            return
        }
        checkAuthentication()
    }

    private fun init() {
        mState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_CALL_STATE, STATE::class.java)
        } else {
            intent.getSerializableExtra(EXTRA_CALL_STATE) as STATE?
        }
        mCallId = intent.getStringExtra(EXTRA_CALL_ID)
        mIsVideoCall = intent.getBooleanExtra(EXTRA_IS_VIDEO_CALL, false)
        mCalleeIdToDial = intent.getStringExtra(EXTRA_CALLEE_ID_TO_DIAL)
        mDoDial = intent.getBooleanExtra(EXTRA_DO_DIAL, false)
        mDoAccept = intent.getBooleanExtra(EXTRA_DO_ACCEPT, false)
        mDoLocalVideoStart = intent.getBooleanExtra(EXTRA_DO_LOCAL_VIDEO_START, false)
        mDoEnd = intent.getBooleanExtra(EXTRA_DO_END, false)
        Log.i(
            TAG,
            "[CallActivity] init() => (mState: " + mState + ", mCallId: " + mCallId + ", mIsVideoCall: " + mIsVideoCall
                    + ", mCalleeIdToDial: " + mCalleeIdToDial + ", mDoDial: " + mDoDial + ", mDoAccept: " + mDoAccept + ", mDoLocalVideoStart: " + mDoLocalVideoStart
                    + ", mDoEnd: " + mDoEnd + ")"
        )
        mCallId?.let { mCallId ->
            mDirectCall = getCall(mCallId)
            setListener(mDirectCall)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(TAG, "[CallActivity] onNewIntent()")
        mDoEnd = intent.getBooleanExtra(EXTRA_DO_END, false)
        if (mDoEnd) {
            Log.i(TAG, "[CallActivity] onNewIntent() => (mDoEnd == true)")
            end()
        }
    }

    protected open fun setViews() {
        mImageViewDecline.setOnClickListener { end() }
        mIsAudioEnabled = if (mDirectCall != null) {
            mDirectCall?.isLocalAudioEnabled ?: false
        } else {
            true
        }
        mImageViewAudioOff.isSelected = !mIsAudioEnabled
        mImageViewAudioOff.setOnClickListener {
            if (mIsAudioEnabled) {
                Log.i(TAG, "[CallActivity] mute()")
                mDirectCall?.muteMicrophone()
                mIsAudioEnabled = false
                mImageViewAudioOff.isSelected = true
            } else {
                Log.i(TAG, "[CallActivity] unmute()")
                mDirectCall?.unmuteMicrophone()
                mIsAudioEnabled = true
                mImageViewAudioOff.isSelected = false
            }
        }
        mImageViewEnd.setOnClickListener { end() }
    }

    private fun setAudioDevice() {
        setAudioDevice(mDirectCall?.currentAudioDevice, mDirectCall?.availableAudioDevices)
    }

    private fun setCurrentState() {
        setState(mState, mDirectCall)
    }

    protected fun setListener(call: DirectCall?) {
        Log.i(TAG, "[CallActivity] setListener()")
        call?.setListener(object : DirectCallListener() {
            override fun onConnected(call: DirectCall) {
                Log.i(TAG, "[CallActivity] onConnected()")
                setState(STATE.STATE_CONNECTED, call)
            }

            override fun onEnded(call: DirectCall) {
                Log.i(TAG, "[CallActivity] onEnded()")
                setState(STATE.STATE_ENDED, call)
                sendCallLogBroadcast(call.callLog)
            }

            override fun onRemoteVideoSettingsChanged(call: DirectCall) {
                Log.i(TAG, "[CallActivity] onRemoteVideoSettingsChanged()")
            }

            override fun onLocalVideoSettingsChanged(call: DirectCall) {
                Log.i(TAG, "[CallActivity] onLocalVideoSettingsChanged()")
                if (this@CallActivity is VideoCallActivity) {
                    setLocalVideoSettings(call)
                }
            }

            override fun onRemoteAudioSettingsChanged(call: DirectCall) {
                Log.i(TAG, "[CallActivity] onRemoteAudioSettingsChanged()")
                setRemoteMuteInfo(call)
            }

            override fun onAudioDeviceChanged(call: DirectCall, currentAudioDevice: AudioDevice?,availableAudioDevices: MutableSet<AudioDevice>) {
                Log.i(TAG,"[CallActivity] onAudioDeviceChanged(currentAudioDevice: $currentAudioDevice, availableAudioDevices: $availableAudioDevices)")
                setAudioDevice(currentAudioDevice, availableAudioDevices)
            }
        })
        call?.setConnectionQualityListener(ConnectionQualityMonitoringMode.CONNECTION_QUALITY_CHANGE) { connectionMetrics ->
            updateNetworkIndicatorState(connectionMetrics.connectionQualityState)
        }
    }

    private fun checkAuthentication() {
        if (currentUser == null) {
            autoAuthenticate(this) { userId ->
                if (userId == null) {
                    finishWithEnding("autoAuthenticate() failed.")
                    return@autoAuthenticate
                }
                ready()
            }
        } else {
            ready()
        }
    }

    private fun ready() {
        if (mDoDial) {
            mDoDial = false
            startCall(false)
        } else if (mDoAccept) {
            mDoAccept = false
            startCall(true)
        }
    }

    protected open fun setState(state: STATE?, call: DirectCall?): Boolean {
        mState = state
        updateCallService()
        when (state) {
            STATE.STATE_ACCEPTING -> {
                mLinearLayoutInfo.visibility = View.VISIBLE
                mLinearLayoutRemoteMute.visibility = View.GONE
                mRelativeLayoutRingingButtons.visibility = View.VISIBLE
                mLinearLayoutConnectingButtons.visibility = View.GONE
                if (mIsVideoCall) {
                    setInfo(call, getString(R.string.calls_incoming_video_call))
                } else {
                    setInfo(call, getString(R.string.calls_incoming_voice_call))
                }
                mImageViewDecline.setBackgroundResource(R.drawable.btn_call_decline)
                setInfo(call, getString(R.string.calls_connecting_call))
            }
            STATE.STATE_OUTGOING -> {
                mLinearLayoutInfo.visibility = View.VISIBLE
                mImageViewProfile.visibility = View.GONE
                mLinearLayoutRemoteMute.visibility = View.GONE
                mRelativeLayoutRingingButtons.visibility = View.GONE
                mLinearLayoutConnectingButtons.visibility = View.VISIBLE
                if (mIsVideoCall) {
                    setInfo(call, getString(R.string.calls_video_calling))
                } else {
                    setInfo(call, getString(R.string.calls_calling))
                }
            }
            STATE.STATE_CONNECTED -> {
                mImageViewProfile.visibility = View.VISIBLE
                mLinearLayoutRemoteMute.visibility = View.VISIBLE
                mRelativeLayoutRingingButtons.visibility = View.GONE
                mLinearLayoutConnectingButtons.visibility = View.VISIBLE
                setRemoteMuteInfo(call)
            }
            STATE.STATE_ENDING -> {
                mLinearLayoutInfo.visibility = View.VISIBLE
                mImageViewProfile.visibility = View.VISIBLE
                mLinearLayoutRemoteMute.visibility = View.GONE
                mRelativeLayoutRingingButtons.visibility = View.GONE
                mLinearLayoutConnectingButtons.visibility = View.GONE
                if (mIsVideoCall) {
                    setInfo(call, getString(R.string.calls_ending_video_call))
                } else {
                    setInfo(call, getString(R.string.calls_ending_voice_call))
                }
            }
            STATE.STATE_ENDED -> {
                mLinearLayoutInfo.visibility = View.VISIBLE
                mImageViewProfile.visibility = View.VISIBLE
                mLinearLayoutRemoteMute.visibility = View.GONE
                mRelativeLayoutRingingButtons.visibility = View.GONE
                mLinearLayoutConnectingButtons.visibility = View.GONE
                val status = if (call != null) getEndResultString(call.endResult) else ""
                setInfo(call, status)
                finishWithEnding(status)
            }
            else -> {}
        }
        return true
    }

    private fun updateNetworkIndicatorState(state: ConnectionQualityState) {
        val (stateName, stateTint) = when (state) {
            ConnectionQualityState.UNAVAILABLE -> {
                mConstraintLayoutNetworkIndicator.visibility = View.GONE
                return
            }
            ConnectionQualityState.POOR, ConnectionQualityState.FAIR -> Pair(getString(R.string.calls_network_indicator_state_poor), R.color.colorPoor)
            ConnectionQualityState.AVERAGE -> Pair(getString(R.string.calls_network_indicator_state_average), R.color.colorAverage)
            ConnectionQualityState.GOOD, ConnectionQualityState.EXCELLENT -> Pair(getString(R.string.calls_network_indicator_state_excellent), R.color.colorExcellent)
        }
        mConstraintLayoutNetworkIndicator.visibility = View.VISIBLE
        mTextViewNetworkIndicator.text = stateName
        mImageViewNetworkIndicator.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, stateTint))
    }

    protected fun setInfo(call: DirectCall?, status: String?) {
        val remoteUser = call?.remoteUser
        setProfileImage(remoteUser, mImageViewProfile)
        mTextViewUserId.text = getRemoteNicknameOrUserId(call)
        mTextViewStatus.visibility = View.VISIBLE
        mTextViewStatus.text = status ?: ""
    }

    private fun getRemoteNicknameOrUserId(call: DirectCall?) = if (call != null) call.remoteUser.getNicknameOrUserId() else mCalleeIdToDial

    private fun setRemoteMuteInfo(call: DirectCall?) {
        if (call != null && !call.isRemoteAudioEnabled && call.remoteUser != null) {
            mTextViewRemoteMute.text = getString(
                R.string.calls_muted_this_call,
                call.remoteUser.getNicknameOrUserId()
            )
            mLinearLayoutRemoteMute.visibility = View.VISIBLE
        } else {
            mLinearLayoutRemoteMute.visibility = View.GONE
        }
    }

    override fun onBackPressed() {}
    private fun end() {
        mDirectCall?.let { mDirectCall ->
            Log.i(TAG, "[CallActivity] end()")
            if (mState == STATE.STATE_ENDING || mState == STATE.STATE_ENDED) {
                Log.i(TAG, "[CallActivity] Already ending call.")
                return
            }
            if (mDirectCall.isEnded) {
                setState(STATE.STATE_ENDED, mDirectCall)
            } else {
                setState(STATE.STATE_ENDING, mDirectCall)
                mDirectCall.end()
            }
        } ?: run {
            Log.i(TAG, "[CallActivity] end() => (mDirectCall == null)")
            finishWithEnding("(mDirectCall == null)")
        }
    }

    protected fun finishWithEnding(log: String) {
        Log.i(TAG, "[CallActivity] finishWithEnding($log)")
        if (mEndingTimer == null) {
            mEndingTimer = Timer().apply {
                schedule(object : TimerTask() {
                    override fun run() {
                        runOnUiThread(Runnable {
                            Log.i(TAG, "[CallActivity] finish()")
                            finish()
                            unbindCallService()
                            stopCallService()
                        })
                    }
                }, ENDING_TIME_MS.toLong())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "[CallActivity] onDestroy()")
        unbindCallService()
    }

    //+ CallService
    private val mCallServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.i(TAG, "[CallActivity] onServiceConnected()")
            val callBinder = iBinder as CallBinder
            mCallService = callBinder.service
            mBound = true
            updateCallService()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.i(TAG, "[CallActivity] onServiceDisconnected()")
            mBound = false
        }
    }

    private fun bindCallService() {
        Log.i(TAG, "[CallActivity] bindCallService()")
        bindService(Intent(this, CallService::class.java), mCallServiceConnection, BIND_AUTO_CREATE)
    }

    private fun unbindCallService() {
        Log.i(TAG, "[CallActivity] unbindCallService()")
        if (mBound) {
            unbindService(mCallServiceConnection)
            mBound = false
        }
    }

    private fun stopCallService() {
        Log.i(TAG, "[CallActivity] stopCallService()")
        CallService.stopService(this)
    }

    protected fun updateCallService() {
        mCallService ?: return
        Log.i(TAG, "[CallActivity] updateCallService()")
        val serviceData = ServiceData()
        serviceData.isHeadsUpNotification = false
        serviceData.remoteNicknameOrUserId = getRemoteNicknameOrUserId(mDirectCall)
        serviceData.callState = mState
        serviceData.callId = if (mDirectCall != null) mDirectCall?.callId else mCallId
        serviceData.isVideoCall = mIsVideoCall
        serviceData.calleeIdToDial = mCalleeIdToDial
        serviceData.doDial = mDoDial
        serviceData.doAccept = mDoAccept
        serviceData.doLocalVideoStart = mDoLocalVideoStart
        mCallService?.updateNotification(serviceData)
    } //- CallService
}

enum class STATE {
    STATE_ACCEPTING, STATE_OUTGOING, STATE_CONNECTED, STATE_ENDING, STATE_ENDED
}
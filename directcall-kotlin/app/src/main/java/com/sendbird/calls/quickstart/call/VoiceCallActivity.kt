package com.sendbird.calls.quickstart.call

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.sendbird.calls.*
import com.sendbird.calls.SendBirdCall.dial
import com.sendbird.calls.quickstart.TAG
import com.sendbird.calls.quickstart.databinding.ActivityVoiceCallBinding
import com.sendbird.calls.quickstart.utils.showToast
import com.sendbird.calls.quickstart.utils.toTimeString
import java.util.*

class VoiceCallActivity : CallActivity() {
    private var mCallDurationTimer: Timer? = null

    private lateinit var _binding: ActivityVoiceCallBinding
    override val binding: ActivityVoiceCallBinding
        get() = _binding

    //region Views
    override val mLinearLayoutInfo: LinearLayout
        get() = binding.linearLayoutInfo
    override val mImageViewProfile: ImageView
        get() = binding.imageViewProfile
    override val mTextViewUserId: TextView
        get() = binding.textViewUserId
    override val mTextViewStatus: TextView
        get() = binding.textViewStatus
    override val mLinearLayoutRemoteMute: LinearLayout
        get() = binding.linearLayoutRemoteMute
    override val mTextViewRemoteMute: TextView
        get() = binding.textViewRemoteMute
    override val mRelativeLayoutRingingButtons: RelativeLayout
        get() = binding.relativeLayoutRingingButtons
    override val mImageViewDecline: ImageView
        get() = binding.imageViewDecline
    override val mLinearLayoutConnectingButtons: LinearLayout
        get() = binding.linearLayoutConnectingButtons
    override val mImageViewAudioOff: ImageView
        get() = binding.imageViewAudioOff
    override val mImageViewBluetooth: ImageView
        get() = binding.imageViewBluetooth
    override val mImageViewEnd: ImageView
        get() = binding.imageViewEnd
    // endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
    }

    override fun setViews() {
        super.setViews()
        binding.imageViewSpeakerphone.setOnClickListener {
            mDirectCall ?: return@setOnClickListener
            binding.imageViewSpeakerphone.isSelected = !binding.imageViewSpeakerphone.isSelected
            if (binding.imageViewSpeakerphone.isSelected) {
                mDirectCall?.selectAudioDevice(AudioDevice.SPEAKERPHONE) { e ->
                    if (e != null) {
                        binding.imageViewSpeakerphone.isSelected = false
                    }
                }
            } else {
                mDirectCall?.selectAudioDevice(AudioDevice.WIRED_HEADSET) { e ->
                    if (e != null) {
                        mDirectCall?.selectAudioDevice(AudioDevice.EARPIECE, null)
                    }
                }
            }
        }
        mImageViewBluetooth.isEnabled = false
        mImageViewBluetooth.setOnClickListener {
            mDirectCall ?: return@setOnClickListener
            mImageViewBluetooth.isSelected = !mImageViewBluetooth.isSelected
            if (mImageViewBluetooth.isSelected) {
                mDirectCall?.selectAudioDevice(AudioDevice.BLUETOOTH) { e ->
                    if (e != null) {
                        mImageViewBluetooth.isSelected = false
                    }
                }
            } else {
                mDirectCall?.selectAudioDevice(AudioDevice.WIRED_HEADSET) { e ->
                    if (e != null) {
                        mDirectCall?.selectAudioDevice(AudioDevice.EARPIECE, null)
                    }
                }
            }
        }
    }

    override fun setAudioDevice(currentAudioDevice: AudioDevice?, availableAudioDevices: Set<AudioDevice>?) {
        when (currentAudioDevice) {
            AudioDevice.SPEAKERPHONE -> {
                binding.imageViewSpeakerphone.isSelected = true
                mImageViewBluetooth.isSelected = false
            }
            AudioDevice.BLUETOOTH -> {
                binding.imageViewSpeakerphone.isSelected = false
                mImageViewBluetooth.isSelected = true
            }
            else -> {
                binding.imageViewSpeakerphone.isSelected = false
            }
        }
        if (availableAudioDevices != null && availableAudioDevices.contains(AudioDevice.SPEAKERPHONE)) {
            binding.imageViewSpeakerphone.isEnabled = true
        } else if (!binding.imageViewSpeakerphone.isSelected) {
            binding.imageViewSpeakerphone.isEnabled = false
        }
        if (availableAudioDevices != null && availableAudioDevices.contains(AudioDevice.BLUETOOTH)) {
            mImageViewBluetooth.isEnabled = true
        } else if (!mImageViewBluetooth.isSelected) {
            mImageViewBluetooth.isEnabled = false
        }
    }

    override fun startCall(amICallee: Boolean) {
        val callOptions = CallOptions().setAudioEnabled(mIsAudioEnabled)
        if (amICallee) {
            Log.i(TAG, "[VoiceCallActivity] accept()")
            mDirectCall?.accept(AcceptParams().setCallOptions(callOptions))
        } else {
            Log.i(TAG, "[VoiceCallActivity] dial()")
            val mCalleeIdToDial = mCalleeIdToDial ?: return
            mDirectCall = dial(DialParams(mCalleeIdToDial).setVideoCall(mIsVideoCall).setCallOptions(callOptions)) { _, e ->
                if (e != null) {
                    Log.i(TAG, "[VoiceCallActivity] dial() => e: " + e.message)
                    if (e.message != null) {
                        showToast(e.message ?: "")
                    }
                    finishWithEnding(e.message ?: "")
                    return@dial
                }
                Log.i(TAG, "[VoiceCallActivity] dial() => OK")
                updateCallService()
            }
            setListener(mDirectCall)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun setState(state: STATE?, call: DirectCall?): Boolean {
        if (!super.setState(state, call)) {
            return false
        }
        when (mState) {
            STATE.STATE_ACCEPTING -> cancelCallDurationTimer()
            STATE.STATE_CONNECTED -> {
                call ?: return false
                setInfo(call, "")
                mLinearLayoutInfo.visibility = View.VISIBLE
                setCallDurationTimer(call)
            }
            STATE.STATE_ENDING, STATE.STATE_ENDED -> cancelCallDurationTimer()
            else -> {}
        }
        return true
    }

    private fun setCallDurationTimer(call: DirectCall) {
        if (mCallDurationTimer == null) {
            mCallDurationTimer = Timer().apply {
                schedule(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            val callDuration = call.duration.toTimeString()
                            mTextViewStatus.text = callDuration
                        }
                    }
                }, 0, 1000)
            }
        }
    }

    private fun cancelCallDurationTimer() {
        mCallDurationTimer ?: return
        mCallDurationTimer?.cancel()
        mCallDurationTimer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "[VoiceCallActivity] onDestroy()")
        cancelCallDurationTimer()
    }
}
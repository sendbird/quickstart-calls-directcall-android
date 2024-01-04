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
import com.sendbird.calls.quickstart.databinding.ActivityVideoCallBinding
import com.sendbird.calls.quickstart.utils.showToast
import org.webrtc.RendererCommon

class VideoCallActivity : CallActivity() {
    private var mIsVideoEnabled = false

    private lateinit var _binding: ActivityVideoCallBinding
    override val binding: ActivityVideoCallBinding
        get() = _binding

    // region Views
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
        _binding = ActivityVideoCallBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
    }

    override fun setViews() {
        super.setViews()
        binding.videoViewFullscreen.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        binding.videoViewFullscreen.setZOrderMediaOverlay(false)
        binding.videoViewFullscreen.setEnableHardwareScaler(true)
        binding.videoViewSmall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        binding.videoViewSmall.setZOrderMediaOverlay(true)
        binding.videoViewSmall.setEnableHardwareScaler(true)
        mDirectCall?.let { mDirectCall ->
            if (mDirectCall.myRole == DirectCallUserRole.CALLER && mState === STATE.STATE_OUTGOING) {
                mDirectCall.setLocalVideoView(binding.videoViewFullscreen)
                mDirectCall.setRemoteVideoView(binding.videoViewSmall)
            } else {
                mDirectCall.setLocalVideoView(binding.videoViewSmall)
                mDirectCall.setRemoteVideoView(binding.videoViewFullscreen)
            }
        }
        binding.imageViewCameraSwitch.setOnClickListener {
            mDirectCall?.switchCamera { e ->
                if (e != null) {
                    Log.i(TAG, "[VideoCallActivity] switchCamera(e: " + e.message + ")")
                }
            }
        }
        mIsVideoEnabled = if (mDirectCall != null && !mDoLocalVideoStart) {
            mDirectCall?.isLocalVideoEnabled ?: false
        } else {
            true
        }
        binding.imageViewVideoOff.isSelected = !mIsVideoEnabled
        binding.imageViewVideoOff.setOnClickListener {
            mDirectCall ?: return@setOnClickListener
            if (mIsVideoEnabled) {
                Log.i(TAG, "[VideoCallActivity] stopVideo()")
                mDirectCall?.stopVideo()
                mIsVideoEnabled = false
                binding.imageViewVideoOff.isSelected = true
            } else {
                Log.i(TAG, "[VideoCallActivity] startVideo()")
                mDirectCall?.startVideo()
                mIsVideoEnabled = true
                binding.imageViewVideoOff.isSelected = false
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
                        mDirectCall?.selectAudioDevice(AudioDevice.SPEAKERPHONE, null)
                    }
                }
            }
        }
    }

    fun setLocalVideoSettings(call: DirectCall) {
        mIsVideoEnabled = call.isLocalVideoEnabled
        Log.i(TAG,"[VideoCallActivity] setLocalVideoSettings() => isLocalVideoEnabled(): $mIsVideoEnabled")
        binding.imageViewVideoOff.isSelected = !mIsVideoEnabled
    }

    override fun setAudioDevice(currentAudioDevice: AudioDevice?, availableAudioDevices: Set<AudioDevice>?) {
        if (currentAudioDevice == AudioDevice.SPEAKERPHONE) {
            mImageViewBluetooth.isSelected = false
        } else if (currentAudioDevice == AudioDevice.BLUETOOTH) {
            mImageViewBluetooth.isSelected = true
        }
        if (availableAudioDevices != null && availableAudioDevices.contains(AudioDevice.BLUETOOTH)) {
            mImageViewBluetooth.isEnabled = true
        } else if (!mImageViewBluetooth.isSelected) {
            mImageViewBluetooth.isEnabled = false
        }
    }

    override fun startCall(amICallee: Boolean) {
        val callOptions = CallOptions().setVideoEnabled(mIsVideoEnabled).setAudioEnabled(mIsAudioEnabled).apply {
            if (amICallee) {
                setLocalVideoView(binding.videoViewSmall).setRemoteVideoView(binding.videoViewFullscreen)
            } else {
                setLocalVideoView(binding.videoViewFullscreen).setRemoteVideoView(binding.videoViewSmall)
            }
        }
        if (amICallee) {
            Log.i(TAG, "[VideoCallActivity] accept()")
            mDirectCall?.accept(AcceptParams().setCallOptions(callOptions))
        } else {
            Log.i(TAG, "[VideoCallActivity] dial()")
            val mCalleeIdToDial = mCalleeIdToDial ?: return
            mDirectCall = dial(DialParams(mCalleeIdToDial).setVideoCall(mIsVideoCall).setCallOptions(callOptions)) { _, e ->
                if (e != null) {
                    Log.i(TAG, "[VideoCallActivity] dial() => e: " + e.message)
                    if (e.message != null) {
                        showToast(e.message ?: "")
                    }
                    finishWithEnding(e.message ?: "")
                    return@dial
                }
                Log.i(TAG, "[VideoCallActivity] dial() => OK")
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
        when (state) {
            STATE.STATE_ACCEPTING -> {
                binding.videoViewFullscreen.visibility = View.GONE
                binding.viewConnectingVideoViewFullscreenFg.visibility = View.GONE
                binding.relativeLayoutVideoViewSmall.visibility = View.GONE
                binding.imageViewCameraSwitch.visibility = View.GONE
            }
            STATE.STATE_OUTGOING -> {
                binding.videoViewFullscreen.visibility = View.VISIBLE
                binding.viewConnectingVideoViewFullscreenFg.visibility = View.VISIBLE
                binding.relativeLayoutVideoViewSmall.visibility = View.GONE
                binding.imageViewCameraSwitch.visibility = View.VISIBLE
                binding.imageViewVideoOff.visibility = View.VISIBLE
            }
            STATE.STATE_CONNECTED -> {
                binding.videoViewFullscreen.visibility = View.VISIBLE
                binding.viewConnectingVideoViewFullscreenFg.visibility = View.GONE
                binding.relativeLayoutVideoViewSmall.visibility = View.VISIBLE
                binding.imageViewCameraSwitch.visibility = View.VISIBLE
                binding.imageViewVideoOff.visibility = View.VISIBLE
                mLinearLayoutInfo.visibility = View.GONE
                if (call != null && call.myRole == DirectCallUserRole.CALLER) {
                    call.setLocalVideoView(binding.videoViewSmall)
                    call.setRemoteVideoView(binding.videoViewFullscreen)
                }
            }
            STATE.STATE_ENDING, STATE.STATE_ENDED -> {
                mLinearLayoutInfo.visibility = View.VISIBLE
                binding.videoViewFullscreen.visibility = View.GONE
                binding.viewConnectingVideoViewFullscreenFg.visibility = View.GONE
                binding.relativeLayoutVideoViewSmall.visibility = View.GONE
                binding.imageViewCameraSwitch.visibility = View.GONE
            }
            else -> {}
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "[VideoCallActivity] onStart()")
        if (mDirectCall != null && mDoLocalVideoStart) {
            mDoLocalVideoStart = false
            updateCallService()
            mDirectCall?.startVideo()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "[VideoCallActivity] onStop()")
        if (mDirectCall != null && mDirectCall?.isLocalVideoEnabled == true) {
            mDirectCall?.stopVideo()
            mDoLocalVideoStart = true
            updateCallService()
        }
    }
}
package com.sendbird.calls.quickstart.main

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.sendbird.calls.quickstart.R
import com.sendbird.calls.quickstart.call.*
import com.sendbird.calls.quickstart.databinding.ActivityFullscreenNotificationBinding

class FullscreenNotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullscreenNotificationBinding

    private var mCallId: String? = null
    private var mDoDial = false
    private var mDoAccept = false
    private var mDoEnd = false
    private var mState: STATE? = null
    private var mIsVideoCall = false
    private var mCalleeIdToDial: String? = null
    private var mDoLocalVideoStart = false
    private var remoteNicknameOrUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        init()
        initView()
    }

    private fun init() {
        mState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_CALL_STATE, STATE::class.java)
        } else {
            intent.getSerializableExtra(EXTRA_CALL_STATE) as STATE?
        }
        mCallId = intent.getStringExtra(EXTRA_CALL_ID)
        remoteNicknameOrUserId = intent.getStringExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID)
        mIsVideoCall = intent.getBooleanExtra(EXTRA_IS_VIDEO_CALL, false)
        mCalleeIdToDial = intent.getStringExtra(EXTRA_CALLEE_ID_TO_DIAL)
        mDoDial = intent.getBooleanExtra(EXTRA_DO_DIAL, false)
        mDoAccept = intent.getBooleanExtra(EXTRA_DO_ACCEPT, false)
        mDoLocalVideoStart = intent.getBooleanExtra(EXTRA_DO_LOCAL_VIDEO_START, false)
        mDoEnd = intent.getBooleanExtra(EXTRA_DO_END, false)
    }
    private fun initView() {
        binding.textViewUserId.text = remoteNicknameOrUserId
        binding.textViewStatus.text = getString(R.string.calls_incoming_video_call)
        binding.imageViewAccept.setOnClickListener {
            val intent = getCallActivityIntent(false)
            startActivity(intent)
            finish()
        }
        binding.imageViewDecline.setOnClickListener {
            val intent = getCallActivityIntent(true)
            startActivity(intent)
            finish()
        }
    }

    private fun getCallActivityIntent(doEnd: Boolean) =
        if (mIsVideoCall) {
            Intent(applicationContext, VideoCallActivity::class.java)
        } else {
            Intent(applicationContext, VoiceCallActivity::class.java)
        }.apply {
            putExtra(EXTRA_CALL_STATE, mState)
            putExtra(EXTRA_CALL_ID, mCallId)
            putExtra(EXTRA_IS_VIDEO_CALL, mIsVideoCall)
            putExtra(EXTRA_CALLEE_ID_TO_DIAL, mCalleeIdToDial)
            putExtra(EXTRA_DO_DIAL, mDoDial)
            putExtra(EXTRA_DO_ACCEPT, mDoAccept)
            putExtra(EXTRA_DO_LOCAL_VIDEO_START, mDoLocalVideoStart)
            putExtra(EXTRA_DO_END, doEnd)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }


    companion object {
        fun getFullScreenNotificationActivityIntent(context: Context, serviceData: ServiceData) =
            Intent(context, FullscreenNotificationActivity::class.java).apply {
                putExtra(EXTRA_IS_VIDEO_CALL, serviceData.isVideoCall)
                putExtra(EXTRA_CALL_STATE, serviceData.callState)
                putExtra(EXTRA_CALL_ID, serviceData.callId)
                putExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID, serviceData.remoteNicknameOrUserId)
                putExtra(EXTRA_IS_VIDEO_CALL, serviceData.isVideoCall)
                putExtra(EXTRA_CALLEE_ID_TO_DIAL, serviceData.calleeIdToDial)
                putExtra(EXTRA_DO_DIAL, serviceData.doDial)
                putExtra(EXTRA_DO_ACCEPT, serviceData.doAccept)
                putExtra(EXTRA_DO_LOCAL_VIDEO_START, serviceData.doLocalVideoStart)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
    }
}
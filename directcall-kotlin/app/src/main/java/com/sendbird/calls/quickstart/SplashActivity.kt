package com.sendbird.calls.quickstart

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.calls.quickstart.utils.AuthenticationUtils
import com.sendbird.calls.quickstart.utils.showToast
import com.sendbird.calls.quickstart.utils.startAuthenticateActivityAndFinish
import com.sendbird.calls.quickstart.utils.startMainActivityAndFinish
import java.util.*

const val SPLASH_TIME_MS = 1000L

class SplashActivity : AppCompatActivity() {
    private var mTimer: Timer? = null
    private var mAutoAuthenticateResult: Boolean = false
    private var mEncodedAuthInfo: String? = null
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (mTimer != null) {
                mTimer?.cancel()
                mTimer = null
            }
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        this.onBackPressedDispatcher.addCallback(backPressedCallback)
        setTimer()
        if (!hasDeepLink()) {
            autoAuthenticate()
        }
    }

    private fun hasDeepLink(): Boolean {
        val data = intent.data ?: return false
        val scheme = data.scheme
        if (scheme != null && scheme == "sendbird") {
            Log.i(TAG, "[SplashActivity] deep link: $data")
            mEncodedAuthInfo = data.host
            return !(mEncodedAuthInfo.isNullOrEmpty())
        }
        return false
    }

    private fun setTimer() {
        mTimer = Timer().apply {
            this.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        mTimer = null
                        if (!mEncodedAuthInfo.isNullOrEmpty()) {
                            AuthenticationUtils.authenticateWithEncodedAuthInfo(this@SplashActivity, mEncodedAuthInfo) { isSuccess, hasInvalidValue ->
                                if (isSuccess) {
                                    startMainActivityAndFinish()
                                } else {
                                    if (hasInvalidValue) {
                                        showToast(getString(R.string.calls_invalid_deep_link))
                                    } else {
                                        showToast(getString(R.string.calls_deep_linking_to_authenticate_failed))
                                    }
                                    finish()
                                }
                            }
                            return@runOnUiThread
                        }
                        if (mAutoAuthenticateResult) {
                            startMainActivityAndFinish()
                        } else {
                            startAuthenticateActivityAndFinish()
                        }
                    }
                }
            }, SPLASH_TIME_MS)
        }
    }

    private fun autoAuthenticate() {
        AuthenticationUtils.autoAuthenticate(this) { userId ->
            if (mTimer != null) {
                mAutoAuthenticateResult = !userId.isNullOrEmpty()
            } else {
                if (userId != null) {
                    startMainActivityAndFinish()
                } else {
                    startAuthenticateActivityAndFinish()
                }
            }
        }
    }
}

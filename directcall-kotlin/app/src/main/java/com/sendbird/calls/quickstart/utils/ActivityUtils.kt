package com.sendbird.calls.quickstart.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import com.sendbird.calls.quickstart.AuthenticateActivity
import com.sendbird.calls.quickstart.SignInManuallyActivity
import com.sendbird.calls.quickstart.TAG
import com.sendbird.calls.quickstart.main.ApplicationInformationActivity
import com.sendbird.calls.quickstart.main.MainActivity

const val START_SIGN_IN_MANUALLY_ACTIVITY_REQUEST_CODE = 1

fun Activity.startAuthenticateActivityAndFinish() {
    Log.i(TAG, "[ActivityUtils] startAuthenticateActivityAndFinish()")
    val intent = Intent(this, AuthenticateActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    startActivity(intent)
    finish()
}

fun Fragment.startAuthenticateActivityAndFinish() {
    requireActivity().startAuthenticateActivityAndFinish()
}

fun Activity.startSignInManuallyActivityForResult() {
    Log.i(TAG, "[ActivityUtils] startSignInManuallyActivityAndFinish()")
    val intent = Intent(this, SignInManuallyActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    startActivityForResult(intent, START_SIGN_IN_MANUALLY_ACTIVITY_REQUEST_CODE)
}

fun Activity.startMainActivityAndFinish() {
    Log.i(TAG, "[ActivityUtils] startMainActivityAndFinish()")
    val intent = Intent(this, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    startActivity(intent)
    finish()
}

fun Activity.startApplicationInformationActivity() {
    Log.i(TAG, "[ActivityUtils] startApplicationInformationActivity()")
    val intent = Intent(this, ApplicationInformationActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    startActivity(intent)
}

fun Fragment.startApplicationInformationActivity() {
    requireActivity().startApplicationInformationActivity()
}

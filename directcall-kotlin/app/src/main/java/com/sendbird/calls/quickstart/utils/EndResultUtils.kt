package com.sendbird.calls.quickstart.utils

import android.content.Context
import com.sendbird.calls.DirectCallEndResult
import com.sendbird.calls.quickstart.R

fun Context.getEndResultString(endResult: DirectCallEndResult?): String {
    return when (endResult) {
        DirectCallEndResult.NONE -> ""
        DirectCallEndResult.NO_ANSWER -> getString(R.string.calls_end_result_no_answer)
        DirectCallEndResult.CANCELED -> getString(R.string.calls_end_result_canceled)
        DirectCallEndResult.DECLINED -> getString(R.string.calls_end_result_declined)
        DirectCallEndResult.COMPLETED -> getString(R.string.calls_end_result_completed)
        DirectCallEndResult.TIMED_OUT -> getString(R.string.calls_end_result_timed_out)
        DirectCallEndResult.CONNECTION_LOST -> getString(R.string.calls_end_result_connection_lost)
        DirectCallEndResult.UNKNOWN -> getString(R.string.calls_end_result_unknown)
        DirectCallEndResult.DIAL_FAILED -> getString(R.string.calls_end_result_dial_failed)
        DirectCallEndResult.ACCEPT_FAILED -> getString(R.string.calls_end_result_accept_failed)
        DirectCallEndResult.OTHER_DEVICE_ACCEPTED -> getString(R.string.calls_end_result_other_device_accepted)
        else -> ""
    }
}

package com.sendbird.calls.quickstart.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.sendbird.calls.DirectCallLog
import com.sendbird.calls.quickstart.TAG

const val INTENT_ACTION_ADD_CALL_LOG = "com.sendbird.calls.quickstart.intent.action.ADD_CALL_LOG"
const val INTENT_EXTRA_CALL_LOG = "call_log"

fun Context.sendCallLogBroadcast(callLog: DirectCallLog?) {
    callLog ?: return
    Log.i(TAG, "[BroadcastUtils] sendCallLogBroadcast()")
    val intent = Intent(INTENT_ACTION_ADD_CALL_LOG)
    intent.putExtra(INTENT_EXTRA_CALL_LOG, callLog)
    sendBroadcast(intent)
}

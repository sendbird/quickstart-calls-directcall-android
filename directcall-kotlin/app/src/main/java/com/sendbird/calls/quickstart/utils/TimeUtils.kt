package com.sendbird.calls.quickstart.utils

import java.text.SimpleDateFormat
import java.util.*


fun Long.toTimeString(): String {
    var totalSec = (this / 1000).toInt()
    var hour = 0
    if (totalSec >= 3600) {
        hour = totalSec / 3600
        totalSec %= 3600
    }
    val min: Int = totalSec / 60
    val sec: Int = totalSec % 60
    return if (hour > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hour, min, sec)
    } else if (min > 0) {
        String.format(Locale.getDefault(), "%d:%02d", min, sec)
    } else {
        String.format(Locale.getDefault(), "0:%02d", sec)
    }
}


fun Long.toTimeStringForHistory(): String {
    var totalSec = (this / 1000).toInt()
    var hour = 0
    if (totalSec >= 3600) {
        hour = totalSec / 3600
        totalSec %= 3600
    }
    val min: Int = totalSec / 60
    val sec: Int = totalSec % 60
    return if (hour > 0) {
        String.format(Locale.getDefault(), "%dh %dm %ds", hour, min, sec)
    } else if (min > 0) {
        String.format(Locale.getDefault(), "%dm %ds", min, sec)
    } else {
        String.format(Locale.getDefault(), "%ds", sec)
    }
}

fun Long.toDateString(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd H:mm", Locale.getDefault())
    val dateString = simpleDateFormat.format(Date(this))
    return dateString.toLowerCase(Locale.getDefault())
}

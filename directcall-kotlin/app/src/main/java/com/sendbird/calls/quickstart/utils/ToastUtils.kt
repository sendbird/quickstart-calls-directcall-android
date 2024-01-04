package com.sendbird.calls.quickstart.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun Fragment.showToast(text: String) {
    requireContext().showToast(text)
}

package com.sendbird.calls.quickstart.utils

import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sendbird.calls.User
import com.sendbird.calls.quickstart.R

fun Context.setProfileImage(user: User?, imageViewProfile: ImageView?) {
    if (user != null && imageViewProfile != null) {
        val profileUrl = user.profileUrl
        if (profileUrl.isNullOrEmpty()) {
            imageViewProfile.setBackgroundResource(R.drawable.icon_avatar)
        } else {
            displayCircularImageFromUrl(user.profileUrl, imageViewProfile)
        }
    }
}

fun Fragment.setProfileImage(user: User?, imageViewProfile: ImageView?) {
    if (user != null && imageViewProfile != null) {
        val profileUrl = user.profileUrl
        if (profileUrl.isNullOrEmpty()) {
            imageViewProfile.setBackgroundResource(R.drawable.icon_avatar)
        } else {
            requireContext().displayCircularImageFromUrl(user.profileUrl, imageViewProfile)
        }
    }
}

fun Context.setNickname(user: User?, textViewNickname: TextView?) {
    if (user != null && textViewNickname != null) {
        val nickname = user.nickname
        if (nickname.isNullOrEmpty()) {
            textViewNickname.text = getString(R.string.calls_empty_nickname)
        } else {
            textViewNickname.text = nickname
        }
    }
}

fun Fragment.setNickname(user: User?, textViewNickname: TextView?) {
    if (user != null && textViewNickname != null) {
        val nickname = user.nickname
        if (nickname.isNullOrEmpty()) {
            textViewNickname.text = getString(R.string.calls_empty_nickname)
        } else {
            textViewNickname.text = nickname
        }
    }
}

fun Context.setUserId(user: User?, textViewUserId: TextView?) {
    if (user != null && textViewUserId != null) {
        textViewUserId.text = getString(R.string.calls_user_id_format, user.userId)
    }
}

fun User?.getNicknameOrUserId(): String? {
    this ?: return null
    return if (!this.nickname.isNullOrEmpty()) this.nickname else this.userId
}

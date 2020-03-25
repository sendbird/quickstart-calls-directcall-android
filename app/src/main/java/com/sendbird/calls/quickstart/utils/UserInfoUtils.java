package com.sendbird.calls.quickstart.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.sendbird.calls.User;
import com.sendbird.calls.quickstart.R;

public class UserInfoUtils {

    public static void setProfileImage(Context context, User user, ImageView imageViewProfile) {
        if (user != null && imageViewProfile != null) {
            String profileUrl = user.getProfileUrl();
            if (TextUtils.isEmpty(profileUrl)) {
                imageViewProfile.setBackgroundResource(R.drawable.icon_avatar);
            } else {
                ImageUtils.displayCircularImageFromUrl(context, user.getProfileUrl(), imageViewProfile);
            }
        }
    }

    public static void setUserId(User user, TextView textViewUserId) {
        if (user != null && textViewUserId != null) {
            textViewUserId.setText(user.getUserId());
        }
    }
}

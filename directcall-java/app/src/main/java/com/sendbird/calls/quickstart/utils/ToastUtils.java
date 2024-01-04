package com.sendbird.calls.quickstart.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

public class ToastUtils {

    public static void showToast(Context context, String text) {
        if (context != null && !TextUtils.isEmpty(text)) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        }
    }
}

package com.sendbird.calls.quickstart.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.sendbird.calls.DirectCall;
import com.sendbird.calls.DirectCallEndResult;
import com.sendbird.calls.User;
import com.sendbird.calls.quickstart.R;

public class EndResultUtils {

    public static String getEndResultString(Context context, DirectCallEndResult endResult) {
        String endResultString = "";
        switch (endResult) {
            case NONE:
                break;
            case NO_ANSWER:
                endResultString = context.getString(R.string.calls_end_result_no_answer);
                break;
            case CANCELED:
                endResultString = context.getString(R.string.calls_end_result_canceled);
                break;
            case DECLINED:
                endResultString = context.getString(R.string.calls_end_result_declined);
                break;
            case COMPLETED:
                endResultString = context.getString(R.string.calls_end_result_completed);
                break;
            case TIMED_OUT:
                endResultString = context.getString(R.string.calls_end_result_timed_out);
                break;
            case CONNECTION_LOST:
                endResultString = context.getString(R.string.calls_end_result_connection_lost);
                break;
            case UNKNOWN:
                endResultString = context.getString(R.string.calls_end_result_unknown);
                break;
            case DIAL_FAILED:
                endResultString = context.getString(R.string.calls_end_result_dial_failed);
                break;
            case ACCEPT_FAILED:
                endResultString = context.getString(R.string.calls_end_result_accept_failed);
                break;
            case OTHER_DEVICE_ACCEPTED:
                endResultString = context.getString(R.string.calls_end_result_other_device_accepted);
                break;
        }
        return endResultString;
    }
}

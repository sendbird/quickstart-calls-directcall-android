package com.sendbird.calls.quickstart.utils;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sendbird.calls.quickstart.R;

//+ [QRCode]
public class QRCodeUtils {

    public static void scanQRCode(Activity activity) {
        new IntentIntegrator(activity)
            .setPrompt(activity.getString(R.string.calls_scanning_a_qrcode_description))
            .setBeepEnabled(false)
            .initiateScan();
    }

    public interface CompletionHandler {
        void onCompletion(boolean isSuccess);
    }

    public static boolean onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data, CompletionHandler handler) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                String contents = result.getContents();

                AuthenticationUtils.authenticateWithEncodedAuthInfo(activity, contents, (isSuccess, hasInvalidValue) -> {
                    if (!isSuccess && hasInvalidValue) {
                        if (resultCode != Activity.RESULT_CANCELED) {
                            ToastUtils.showToast(activity, activity.getString(R.string.calls_invalid_qrcode));
                        }
                    }

                    if (handler != null) {
                        handler.onCompletion(isSuccess);
                    }
                });
            } else {
                if (handler != null) {
                    handler.onCompletion(false);
                }
            }
            return true;
        }

        if (handler != null) {
            handler.onCompletion(false);
        }
        return false;
    }
}
//- [QRCode]
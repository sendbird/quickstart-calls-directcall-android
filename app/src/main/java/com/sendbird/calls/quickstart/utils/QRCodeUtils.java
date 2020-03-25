package com.sendbird.calls.quickstart.utils;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sendbird.calls.quickstart.BaseApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

//+ [QRCode]
public class QRCodeUtils {

    public static void scanQRCode(Activity activity) {
        new IntentIntegrator(activity)
            .setPrompt("")
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
                String appId = null;
                String userId = null;
                String accessToken = null;

                try {
                    if (!TextUtils.isEmpty(contents)) {
                        String jsonString = new String(Base64.decode(contents, Base64.DEFAULT), "UTF-8");
                        JSONObject jsonObject = new JSONObject(jsonString);
                        appId = jsonObject.getString("app_id");
                        userId = jsonObject.getString("user_id");
                        accessToken = jsonObject.getString("access_token");
                    }
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }

                if (!TextUtils.isEmpty(appId) && !TextUtils.isEmpty(userId)
                        && ((BaseApplication)activity.getApplication()).initSendBirdCall(appId)) {
                    AuthenticationUtils.authenticate(activity, userId, accessToken, isSuccess -> {
                        if (handler != null) {
                            handler.onCompletion(isSuccess);
                        }
                    });
                } else {
                    if (handler != null) {
                        handler.onCompletion(false);
                    }
                }
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
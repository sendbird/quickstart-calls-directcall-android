package com.sendbird.call.sample.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.sendbird.call.AuthenticateParams;
import com.sendbird.call.SendBirdCall;
import com.sendbird.call.sample.BaseApplication;

public class LoginUtils {

    public interface LoginHandler {
        void onResult(boolean isSuccess);
    }

    public static void login(Context context, String userId, LoginHandler handler) {
        Log.e(BaseApplication.TAG, "[LoginUtils] login()");

        if (userId == null) {
            Log.e(BaseApplication.TAG, "[LoginUtils] login() => Failed (userId == null)");
            if (handler != null) {
                handler.onResult(false);
            }
            return;
        }

        PushUtils.getPushToken(context, (pushToken, e) -> {
            if (e != null) {
                Log.e(BaseApplication.TAG, "[LoginUtils] login() => Failed (e: " + e.getMessage() + ")");
                if (handler != null) {
                    handler.onResult(false);
                }
                return;
            }

            Log.e(BaseApplication.TAG, "[LoginUtils] login() => authenticate()");
            SendBirdCall.authenticate(new AuthenticateParams(userId).setPushToken(pushToken, false), (user, e1) -> {
                if (e1 != null) {
                    Log.e(BaseApplication.TAG, "[LoginUtils] login() => authenticate() => Failed (e1: " + e1.getMessage() + ")");
                    if (handler != null) {
                        handler.onResult(false);
                    }
                    return;
                }

                PrefUtils.setUserId(context, userId);
                PrefUtils.setPushToken(context, pushToken);

                Log.e(BaseApplication.TAG, "[LoginUtils] login() => authenticate() => OK");
                if (handler != null) {
                    handler.onResult(true);
                }
            });
        });
    }

    public interface LogoutHandler {
        void onResult(boolean isSuccess);
    }

    public static void logout(Context context, LogoutHandler handler) {
        Log.e(BaseApplication.TAG, "[LoginUtils] logout()");

        String pushToken = PrefUtils.getPushToken(context);
        SendBirdCall.deauthenticate(pushToken, e -> {
            if (e != null) {
                Log.e(BaseApplication.TAG, "[LoginUtils] logout() => Failed (e: " + e.getMessage() + ")");
            } else {
                Log.e(BaseApplication.TAG, "[LoginUtils] logout() => OK");
            }

            PrefUtils.setUserId(context, null);
            PrefUtils.setPushToken(context, null);

            if (handler != null) {
                handler.onResult(e == null);
            }
        });
    }

    public interface AutoLoginHandler {
        void onResult(String userId);
    }

    public static void autoLogin(Context context, AutoLoginHandler handler) {
        Log.e(BaseApplication.TAG, "[LoginUtils] autoLogin()");

        if (SendBirdCall.getCurrentUser() != null) {
            Log.e(BaseApplication.TAG, "[LoginUtils] autoLogin() => OK (SendBirdCall.getCurrentUser() != null)");
            if (handler != null) {
                handler.onResult(SendBirdCall.getCurrentUser().getUserId());
            }
            return;
        }

        String userId = PrefUtils.getUserId(context);
        String pushToken = PrefUtils.getPushToken(context);
        if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(pushToken)) {
            Log.e(BaseApplication.TAG, "[LoginUtils] autoLogin() => authenticate()");
                SendBirdCall.authenticate(new AuthenticateParams(userId).setPushToken(pushToken, false), (user, e) -> {
                if (e != null) {
                    Log.e(BaseApplication.TAG, "[LoginUtils] autoLogin() => authenticate() => e: " + e.getMessage());
                    if (handler != null) {
                        handler.onResult(null);
                    }
                    return;
                }

                Log.e(BaseApplication.TAG, "[LoginUtils] autoLogin() => authenticate() => OK (Authenticated)");
                if (handler != null) {
                    handler.onResult(userId);
                }
            });
        } else {
            Log.e(BaseApplication.TAG, "[LoginUtils] autoLogin() => Failed (No userId and pushToken)");
            if (handler != null) {
                handler.onResult(null);
            }
        }
    }
}

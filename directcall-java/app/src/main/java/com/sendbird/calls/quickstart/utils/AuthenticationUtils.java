package com.sendbird.calls.quickstart.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.sendbird.calls.AuthenticateParams;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.SendBirdException;
import com.sendbird.calls.quickstart.BaseApplication;
import com.sendbird.calls.quickstart.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class AuthenticationUtils {

    public interface AuthenticateHandler {
        void onResult(boolean isSuccess);
    }

    public static void authenticate(Context context, String userId, String accessToken, AuthenticateHandler handler) {
        if (userId == null) {
            Log.i(BaseApplication.TAG, "[AuthenticationUtils] authenticate() => Failed (userId == null)");
            if (handler != null) {
                handler.onResult(false);
            }
            return;
        }

        deauthenticate(context, isSuccess -> {
            PushUtils.getPushToken(context, (pushToken, e) -> {
                if (e != null) {
                    Log.i(BaseApplication.TAG, "[AuthenticationUtils] authenticate() => Failed (e: " + e.getMessage() + ")");
                    if (handler != null) {
                        handler.onResult(false);
                    }
                    return;
                }

                Log.i(BaseApplication.TAG, "[AuthenticationUtils] authenticate(userId: " + userId + ")");
                SendBirdCall.authenticate(new AuthenticateParams(userId).setAccessToken(accessToken), (user, e1) -> {
                    if (e1 != null) {
                        Log.i(BaseApplication.TAG, "[AuthenticationUtils] authenticate() => Failed (e1: " + e1.getMessage() + ")");
                        showToastErrorMessage(context, e1);

                        if (handler != null) {
                            handler.onResult(false);
                        }
                        return;
                    }

                    Log.i(BaseApplication.TAG, "[AuthenticationUtils] authenticate() => registerPushToken(pushToken: " + pushToken + ")");
                    SendBirdCall.registerPushToken(pushToken, false, e2 -> {
                        if (e2 != null) {
                            Log.i(BaseApplication.TAG, "[AuthenticationUtils] authenticate() => registerPushToken() => Failed (e2: " + e2.getMessage() + ")");
                            showToastErrorMessage(context, e2);

                            if (handler != null) {
                                handler.onResult(false);
                            }
                            return;
                        }

                        PrefUtils.setAppId(context, SendBirdCall.getApplicationId());
                        PrefUtils.setUserId(context, userId);
                        PrefUtils.setAccessToken(context, accessToken);
                        PrefUtils.setPushToken(context, pushToken);

                        Log.i(BaseApplication.TAG, "[AuthenticationUtils] authenticate() => OK");
                        if (handler != null) {
                            handler.onResult(true);
                        }
                    });
                });
            });
        });
    }

    public interface CompletionWithDetailHandler {
        void onCompletion(boolean isSuccess, boolean hasInvalidValue);
    }

    public static void authenticateWithEncodedAuthInfo(Activity activity, String encodedAuthInfo, CompletionWithDetailHandler handler) {
        String appId = null;
        String userId = null;
        String accessToken = null;

        try {
            if (!TextUtils.isEmpty(encodedAuthInfo)) {
                String jsonString = new String(Base64.decode(encodedAuthInfo, Base64.DEFAULT), "UTF-8");
                JSONObject jsonObject = new JSONObject(jsonString);
                appId = jsonObject.getString("app_id");
                userId = jsonObject.getString("user_id");
                accessToken = jsonObject.getString("access_token");
            }
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(appId) && !TextUtils.isEmpty(userId)
                && ((BaseApplication) activity.getApplication()).initSendBirdCall(appId)) {
            AuthenticationUtils.authenticate(activity, userId, accessToken, isSuccess -> {
                if (handler != null) {
                    handler.onCompletion(isSuccess, false);
                }
            });
        } else {
            if (handler != null) {
                handler.onCompletion(false, true);
            }
        }
    }

    public interface DeauthenticateHandler {
        void onResult(boolean isSuccess);
    }

    public static void deauthenticate(Context context, DeauthenticateHandler handler) {
        if (SendBirdCall.getCurrentUser() == null) {
            if (handler != null) {
                handler.onResult(false);
            }
            return;
        }

        Log.i(BaseApplication.TAG, "[AuthenticationUtils] deauthenticate(userId: " + SendBirdCall.getCurrentUser().getUserId() + ")");
        String pushToken = PrefUtils.getPushToken(context);
        if (!TextUtils.isEmpty(pushToken)) {
            Log.i(BaseApplication.TAG, "[AuthenticationUtils] deauthenticate() => unregisterPushToken(pushToken: " + pushToken + ")");
            SendBirdCall.unregisterPushToken(pushToken, e -> {
                if (e != null) {
                    Log.i(BaseApplication.TAG, "[AuthenticationUtils] unregisterPushToken() => Failed (e: " + e.getMessage() + ")");
                    showToastErrorMessage(context, e);
                }

                doDeauthenticate(context, handler);
            });
        } else {
            doDeauthenticate(context, handler);
        }
    }

    private static void doDeauthenticate(Context context, DeauthenticateHandler handler) {
        SendBirdCall.deauthenticate(e -> {
            if (e != null) {
                Log.i(BaseApplication.TAG, "[AuthenticationUtils] deauthenticate() => Failed (e: " + e.getMessage() + ")");
                showToastErrorMessage(context, e);
            } else {
                Log.i(BaseApplication.TAG, "[AuthenticationUtils] deauthenticate() => OK");
            }

            PrefUtils.setUserId(context, null);
            PrefUtils.setAccessToken(context, null);
            PrefUtils.setCalleeId(context, null);
            PrefUtils.setPushToken(context, null);

            if (handler != null) {
                handler.onResult(e == null);
            }
        });
    }

    public interface AutoAuthenticateHandler {
        void onResult(String userId);
    }

    public static void autoAuthenticate(Context context, AutoAuthenticateHandler handler) {
        Log.i(BaseApplication.TAG, "[AuthenticationUtils] autoAuthenticate()");

        if (SendBirdCall.getCurrentUser() != null) {
            Log.i(BaseApplication.TAG, "[AuthenticationUtils] autoAuthenticate(userId: " + SendBirdCall.getCurrentUser().getUserId() + ") => OK (SendBirdCall.getCurrentUser() != null)");
            if (handler != null) {
                handler.onResult(SendBirdCall.getCurrentUser().getUserId());
            }
            return;
        }

        String userId = PrefUtils.getUserId(context);
        String accessToken = PrefUtils.getAccessToken(context);
        String pushToken = PrefUtils.getPushToken(context);
        if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(pushToken)) {
            Log.i(BaseApplication.TAG, "[AuthenticationUtils] autoAuthenticate() => authenticate(userId: " + userId + ")");
            SendBirdCall.authenticate(new AuthenticateParams(userId).setAccessToken(accessToken), (user, e) -> {
                if (e != null) {
                    Log.i(BaseApplication.TAG, "[AuthenticationUtils] autoAuthenticate() => authenticate() => Failed (e: " + e.getMessage() + ")");
                    showToastErrorMessage(context, e);

                    if (handler != null) {
                        handler.onResult(null);
                    }
                    return;
                }

                Log.i(BaseApplication.TAG, "[AuthenticationUtils] autoAuthenticate() => registerPushToken(pushToken: " + pushToken + ")");
                SendBirdCall.registerPushToken(pushToken, false, e1 -> {
                    if (e1 != null) {
                        Log.i(BaseApplication.TAG, "[AuthenticationUtils] autoAuthenticate() => registerPushToken() => Failed (e1: " + e1.getMessage() + ")");
                        showToastErrorMessage(context, e1);

                        if (handler != null) {
                            handler.onResult(null);
                        }
                        return;
                    }

                    Log.i(BaseApplication.TAG, "[AuthenticationUtils] autoAuthenticate() => authenticate() => OK (Authenticated)");
                    if (handler != null) {
                        handler.onResult(userId);
                    }
                });
            });
        } else {
            Log.i(BaseApplication.TAG, "[AuthenticationUtils] autoAuthenticate() => Failed (No userId and pushToken)");
            if (handler != null) {
                handler.onResult(null);
            }
        }
    }

    private static void showToastErrorMessage(Context context, SendBirdException e) {
        if (context != null) {
            if (e.getCode() == 400111) {
                ToastUtils.showToast(context, context.getString(R.string.calls_invalid_notifications_setting_in_dashboard));
            } else {
                ToastUtils.showToast(context, e.getMessage());
            }
        }
    }
}

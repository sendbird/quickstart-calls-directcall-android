package com.sendbird.calls.quickstart;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.calls.quickstart.utils.ActivityUtils;
import com.sendbird.calls.quickstart.utils.AuthenticationUtils;
import com.sendbird.calls.quickstart.utils.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_MS = 1000;

    private Context mContext;
    private Timer mTimer;
    private Boolean mAutoAuthenticateResult;
    private String mEncodedAuthInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContext = this;

        setTimer();

        if (!hasDeepLink()) {
            autoAuthenticate();
        }
    }

    private boolean hasDeepLink() {
        boolean result = false;

        Intent intent = getIntent();
        if (intent != null) {
            Uri data = intent.getData();
            if (data != null) {
                String scheme = data.getScheme();
                if (scheme != null && scheme.equals("sendbird")) {
                    Log.i(BaseApplication.TAG, "[SplashActivity] deep link: " + data.toString());
                    mEncodedAuthInfo = data.getHost();
                    if (!TextUtils.isEmpty(mEncodedAuthInfo)) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    private void setTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    mTimer = null;

                    if (!TextUtils.isEmpty(mEncodedAuthInfo)) {
                        AuthenticationUtils.authenticateWithEncodedAuthInfo(SplashActivity.this, mEncodedAuthInfo, (isSuccess, hasInvalidValue) -> {
                            if (isSuccess) {
                                ActivityUtils.startMainActivityAndFinish(SplashActivity.this);
                            } else {
                                if (hasInvalidValue) {
                                    ToastUtils.showToast(SplashActivity.this, getString(R.string.calls_invalid_deep_link));
                                } else {
                                    ToastUtils.showToast(SplashActivity.this, getString(R.string.calls_deep_linking_to_authenticate_failed));
                                }
                                finish();
                            }
                        });
                        return;
                    }

                    if (mAutoAuthenticateResult != null) {
                        if (mAutoAuthenticateResult) {
                            ActivityUtils.startMainActivityAndFinish(SplashActivity.this);
                        } else {
                            ActivityUtils.startAuthenticateActivityAndFinish(SplashActivity.this);
                        }
                    }
                });
            }
        }, SPLASH_TIME_MS);
    }

    private void autoAuthenticate() {
        AuthenticationUtils.autoAuthenticate(mContext, userId -> {
            if (mTimer != null) {
                mAutoAuthenticateResult = !TextUtils.isEmpty(userId);
            } else {
                if (userId != null) {
                    ActivityUtils.startMainActivityAndFinish(SplashActivity.this);
                } else {
                    ActivityUtils.startAuthenticateActivityAndFinish(SplashActivity.this);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        super.onBackPressed();
    }
}

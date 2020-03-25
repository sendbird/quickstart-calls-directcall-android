package com.sendbird.calls.quickstart;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.calls.quickstart.utils.ActivityUtils;
import com.sendbird.calls.quickstart.utils.AuthenticationUtils;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_MS = 1000;

    private Context mContext;
    private Timer mTimer;
    private Boolean mAutoAuthenticateResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContext = this;

        setTimer();
        autoAuthenticate();
    }

    private void setTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    mTimer = null;
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

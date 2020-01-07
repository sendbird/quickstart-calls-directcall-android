package com.sendbird.call.sample;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.call.SendBirdCall;
import com.sendbird.call.sample.utils.LoginUtils;
import com.sendbird.call.sample.utils.PrefUtils;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private EditText mEditTextUserId;
    private Button mButtonLogin;
    private Button mButtonLogout;

    private LinearLayout mLinearLayoutCall;
    private EditText mEditTextCalleeId;
    private Button mButtonAudioCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setViews();
        autoLogin();
    }

    private void initViews() {
        mContext = this;

        mEditTextUserId = findViewById(R.id.edit_text_user_id);
        mButtonLogin = findViewById(R.id.button_login);
        mButtonLogout = findViewById(R.id.button_logout);

        mLinearLayoutCall = findViewById(R.id.linear_layout_call);
        mEditTextCalleeId = findViewById(R.id.edit_text_callee_id);
        mButtonAudioCall = findViewById(R.id.button_audio_call);

        ((TextView) findViewById(R.id.text_view_sdk_version)).setText("[SendBird Calls] Sample " + BaseApplication.VERSION + " / SDK " + SendBirdCall.getSdkVersion());
    }

    private void setViews() {
        mButtonLogin.setEnabled(false);
        mButtonLogout.setEnabled(false);
        mLinearLayoutCall.setVisibility(View.INVISIBLE);

        String savedCalleeId = PrefUtils.getCalleeId(mContext);
        if (!TextUtils.isEmpty(savedCalleeId)) {
            mEditTextCalleeId.setText(savedCalleeId);
        }

        mButtonLogin.setOnClickListener(view -> {
            String userId = mEditTextUserId.getText().toString();
            if (!TextUtils.isEmpty(userId)) {
                LoginUtils.login(mContext, userId, isSuccess -> {
                    if (isSuccess) {
                        mEditTextUserId.setEnabled(false);
                        mButtonLogin.setEnabled(false);
                        mButtonLogout.setEnabled(true);
                        mLinearLayoutCall.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        mButtonLogout.setOnClickListener(view -> {
            LoginUtils.logout(mContext, isSuccess -> {
                mEditTextUserId.setEnabled(true);
                mButtonLogin.setEnabled(true);
                mButtonLogout.setEnabled(false);
                mLinearLayoutCall.setVisibility(View.INVISIBLE);
            });
        });

        mButtonAudioCall.setOnClickListener(view -> {
            String calleeId = mEditTextCalleeId.getText().toString();
            if (!TextUtils.isEmpty(calleeId)) {
                CallActivity.startAsCaller(mContext, calleeId);

                PrefUtils.setCalleeId(mContext, calleeId);
            }
        });
    }

    private void autoLogin() {
        LoginUtils.autoLogin(mContext, userId -> {
            if (userId == null) {
                mButtonLogin.setEnabled(true);
                mButtonLogout.setEnabled(false);
                mLinearLayoutCall.setVisibility(View.INVISIBLE);
                return;
            }

            mEditTextUserId.setText(userId);
            mButtonLogin.setEnabled(false);
            mButtonLogout.setEnabled(true);
            mLinearLayoutCall.setVisibility(View.VISIBLE);
        });
    }
}
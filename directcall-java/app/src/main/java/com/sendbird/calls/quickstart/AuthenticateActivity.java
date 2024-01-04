package com.sendbird.calls.quickstart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.quickstart.utils.ActivityUtils;
import com.sendbird.calls.quickstart.utils.QRCodeUtils;

public class AuthenticateActivity extends AppCompatActivity {

    private RelativeLayout mRelativeLayoutSignInWithQRCode;
    private RelativeLayout mRelativeLayoutSignInManually;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);

        initViews();
    }

    private void initViews() {
        //+ [QRCode]
        mRelativeLayoutSignInWithQRCode = findViewById(R.id.relative_layout_sign_in_with_qrcode);
        mRelativeLayoutSignInWithQRCode.setOnClickListener(view -> {
            mRelativeLayoutSignInWithQRCode.setEnabled(false);
            mRelativeLayoutSignInManually.setEnabled(false);

            QRCodeUtils.scanQRCode(AuthenticateActivity.this);
        });
        //- [QRCode]

        mRelativeLayoutSignInManually = findViewById(R.id.relative_layout_sign_in_manually);
        mRelativeLayoutSignInManually.setOnClickListener(view -> {
            ActivityUtils.startSignInManuallyActivityForResult(AuthenticateActivity.this);
        });

        ((TextView)findViewById(R.id.text_view_quickstart_version)).setText(getString(R.string.calls_quickstart_version, BaseApplication.VERSION));
        ((TextView)findViewById(R.id.text_view_sdk_version)).setText(getString(R.string.calls_sdk_version, SendBirdCall.getSdkVersion()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ActivityUtils.START_SIGN_IN_MANUALLY_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                finish();
            }
            return;
        }

        //+ [QRCode]
        if (QRCodeUtils.onActivityResult(AuthenticateActivity.this, requestCode, resultCode, data, isSuccess -> {
            if (isSuccess) {
                ActivityUtils.startMainActivityAndFinish(AuthenticateActivity.this);
            } else {
                mRelativeLayoutSignInWithQRCode.setEnabled(true);
                mRelativeLayoutSignInManually.setEnabled(true);
            }
        })) {
            return;
        }
        //- [QRCode]
    }
}

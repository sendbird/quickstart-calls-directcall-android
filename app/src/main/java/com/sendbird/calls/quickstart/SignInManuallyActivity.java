package com.sendbird.calls.quickstart;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sendbird.calls.quickstart.utils.ActivityUtils;
import com.sendbird.calls.quickstart.utils.AuthenticationUtils;
import com.sendbird.calls.quickstart.utils.PrefUtils;

public class SignInManuallyActivity extends AppCompatActivity {

    private Context mContext;
    private InputMethodManager mInputMethodManager;

    private TextInputLayout mTextInputLayoutAppId;
    private TextInputEditText mTextInputEditTextAppId;
    private TextInputLayout mTextInputLayoutUserId;
    private TextInputEditText mTextInputEditTextUserId;
    private TextInputLayout mTextInputLayoutAccessToken;
    private TextInputEditText mTextInputEditTextAccessToken;
    private RelativeLayout mRelativeLayoutSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_manually);

        mContext = this;
        mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        initViews();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.calls_sign_in_manually_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_close);
        }

        mTextInputLayoutAppId = findViewById(R.id.text_input_layout_app_id);
        mTextInputEditTextAppId = findViewById(R.id.text_input_edit_text_app_id);
        mTextInputLayoutUserId = findViewById(R.id.text_input_layout_user_id);
        mTextInputEditTextUserId = findViewById(R.id.text_input_edit_text_user_id);
        mTextInputLayoutAccessToken = findViewById(R.id.text_input_layout_access_token);
        mTextInputEditTextAccessToken = findViewById(R.id.text_input_edit_text_access_token);
        mRelativeLayoutSignIn = findViewById(R.id.relative_layout_sign_in);

        String savedAppId = PrefUtils.getAppId(mContext);
        if (!TextUtils.isEmpty(savedAppId)) {
            if (!savedAppId.equals("YOUR_APPLICATION_ID")) {
                mTextInputEditTextAppId.setText(savedAppId);
            }
        }
        String savedUserId = PrefUtils.getUserId(mContext);
        if (!TextUtils.isEmpty(savedUserId)) {
            mTextInputEditTextUserId.setText(savedUserId);
        }
        String savedAccessToken = PrefUtils.getAccessToken(mContext);
        if (!TextUtils.isEmpty(savedAccessToken)) {
            mTextInputEditTextAccessToken.setText(savedAccessToken);
        }
        checkSignInStatus();

        mTextInputEditTextAppId.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mTextInputEditTextAppId.clearFocus();
                mInputMethodManager.hideSoftInputFromWindow(mTextInputEditTextAppId.getWindowToken(), 0);
                return true;
            }
            return false;
        });
        mTextInputEditTextAppId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkSignInStatus();
            }
        });

        mTextInputEditTextUserId.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mTextInputEditTextUserId.clearFocus();
                mInputMethodManager.hideSoftInputFromWindow(mTextInputEditTextUserId.getWindowToken(), 0);
                return true;
            }
            return false;
        });
        mTextInputEditTextUserId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkSignInStatus();
            }
        });

        mTextInputEditTextAccessToken.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mTextInputEditTextAccessToken.clearFocus();
                mInputMethodManager.hideSoftInputFromWindow(mTextInputEditTextAccessToken.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        mRelativeLayoutSignIn.setOnClickListener(view -> {
            String appId = "";
            String userId = "";
            String accessToken = "";

            if (mTextInputEditTextAppId != null) {
                appId = (mTextInputEditTextAppId.getText() != null ? mTextInputEditTextAppId.getText().toString() : "");
            }
            if (mTextInputEditTextUserId != null) {
                userId = (mTextInputEditTextUserId.getText() != null ? mTextInputEditTextUserId.getText().toString() : "");
            }
            if (mTextInputEditTextAccessToken != null) {
                accessToken = (mTextInputEditTextAccessToken.getText() != null ? mTextInputEditTextAccessToken.getText().toString() : "");
            }

            if (!TextUtils.isEmpty(appId) && !TextUtils.isEmpty(userId)
                    && ((BaseApplication)getApplication()).initSendBirdCall(appId)) {
                AuthenticationUtils.authenticate(mContext, userId, accessToken, isSuccess -> {
                    if (isSuccess) {
                        setResult(RESULT_OK, null);
                        ActivityUtils.startMainActivityAndFinish(SignInManuallyActivity.this);
                    } else {
                        mTextInputLayoutAppId.setEnabled(true);
                        mTextInputLayoutUserId.setEnabled(true);
                        mTextInputLayoutAccessToken.setEnabled(true);
                        mRelativeLayoutSignIn.setEnabled(true);
                    }
                });

                mTextInputLayoutAppId.setEnabled(false);
                mTextInputLayoutUserId.setEnabled(false);
                mTextInputLayoutAccessToken.setEnabled(false);
                mRelativeLayoutSignIn.setEnabled(false);
            }
        });
    }

    private void checkSignInStatus() {
        if (mTextInputEditTextAppId != null && mTextInputEditTextUserId != null) {
            String appId = (mTextInputEditTextAppId.getText() != null ? mTextInputEditTextAppId.getText().toString() : "");
            String userId = (mTextInputEditTextUserId.getText() != null ? mTextInputEditTextUserId.getText().toString() : "");

            if (!TextUtils.isEmpty(appId) && !TextUtils.isEmpty(userId)) {
                mRelativeLayoutSignIn.setEnabled(true);
            } else {
                mRelativeLayoutSignIn.setEnabled(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

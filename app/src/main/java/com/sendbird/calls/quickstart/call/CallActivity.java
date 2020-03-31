package com.sendbird.calls.quickstart.call;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.calls.AudioDevice;
import com.sendbird.calls.DirectCall;
import com.sendbird.calls.DirectCallUser;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.handler.DirectCallListener;
import com.sendbird.calls.quickstart.R;
import com.sendbird.calls.quickstart.utils.ActivityUtils;
import com.sendbird.calls.quickstart.utils.AuthenticationUtils;
import com.sendbird.calls.quickstart.utils.UserInfoUtils;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public abstract class CallActivity extends AppCompatActivity {

    public static boolean sIsRunning;

    private static final String TAG = "CallActivity";

    static final int ENDING_TIME_MS = 1000;
    static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    enum STATE {
        STATE_INCOMING,
        STATE_ACCEPTING,
        STATE_OUTGOING,
        STATE_CONNECTED,
        STATE_ENDING,
        STATE_ENDED
    }

    Context mContext;
    private String mIncomingCallId;
    private Timer mEndingTimer;

    STATE mState;
    String mCalleeId;
    boolean mIsVideoCall;
    DirectCall mDirectCall;
    boolean mIsAudioEnabled = true;

    //+ Views
    LinearLayout mLinearLayoutInfo;
    ImageView mImageViewProfile;
    TextView mTextViewUserId;
    TextView mTextViewStatus;

    LinearLayout mLinearLayoutRemoteMute;
    TextView mTextViewRemoteMute;

    RelativeLayout mRelativeLayoutRingingButtons;
    ImageView mImageViewDecline;
    ImageView mImageViewAccept;

    LinearLayout mLinearLayoutConnectingButtons;
    ImageView mImageViewAudioOff;
    ImageView mImageViewBluetooth;
    ImageView mImageViewEnd;
    //- Views

    //+ abstract methods
    protected abstract int getLayoutResourceId();
    protected abstract String[] getMandatoryPermissions();
    protected abstract void audioDeviceChanged(DirectCall call, AudioDevice currentAudioDevice, Set<AudioDevice> availableAudioDevices);
    protected abstract void startCall(boolean amICallee);
    //- abstract methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(getLayoutResourceId());

        mContext = this;
        sIsRunning = true;

        initViews();
        setViews();

        mIncomingCallId = getIntent().getStringExtra(ActivityUtils.EXTRA_INCOMING_CALL_ID);
        if (mIncomingCallId != null) {  // as callee
            mDirectCall = SendBirdCall.getCall(mIncomingCallId);
            mCalleeId = mDirectCall.getCallee().getUserId();
            mIsVideoCall = mDirectCall.isVideoCall();

            setListener(mDirectCall);
        } else {    // as caller
            mCalleeId = getIntent().getStringExtra(ActivityUtils.EXTRA_CALLEE_ID);
            mIsVideoCall = getIntent().getBooleanExtra(ActivityUtils.EXTRA_IS_VIDEO_CALL, false);
        }

        if (setInitialState()) {
            checkAuthenticate();
        }
    }

    protected void initViews() {
        mLinearLayoutInfo = findViewById(R.id.linear_layout_info);
        mImageViewProfile = findViewById(R.id.image_view_profile);
        mTextViewUserId = findViewById(R.id.text_view_user_id);
        mTextViewStatus = findViewById(R.id.text_view_status);

        mLinearLayoutRemoteMute = findViewById(R.id.linear_layout_remote_mute);
        mTextViewRemoteMute = findViewById(R.id.text_view_remote_mute);

        mRelativeLayoutRingingButtons = findViewById(R.id.relative_layout_ringing_buttons);
        mImageViewDecline = findViewById(R.id.image_view_decline);
        mImageViewAccept = findViewById(R.id.image_view_accept);

        mLinearLayoutConnectingButtons = findViewById(R.id.linear_layout_connecting_buttons);
        mImageViewAudioOff = findViewById(R.id.image_view_audio_off);
        mImageViewBluetooth = findViewById(R.id.image_view_bluetooth);
        mImageViewEnd = findViewById(R.id.image_view_end);
    }

    protected void setViews() {
        mImageViewDecline.setOnClickListener(view -> {
            if (mDirectCall != null) {
                end(mDirectCall);
            }
        });

        mImageViewAccept.setOnClickListener(view -> {
            if (SendBirdCall.getCurrentUser() == null) {
                Log.d(TAG, "mImageViewAccept clicked => (SendBirdCall.getCurrentUser() == null)");
                return;
            }

            if (mState == STATE.STATE_ENDING) {
                Log.d(TAG, "mImageViewAccept clicked => Already ending call.");
                return;
            }

            if (mState == STATE.STATE_ACCEPTING) {
                Log.d(TAG, "mImageViewAccept clicked => Already accepting call.");
                return;
            }

            setState(STATE.STATE_ACCEPTING, mDirectCall);
            startCall(true);
        });

        if (mIsAudioEnabled) {
            mImageViewAudioOff.setSelected(false);
        } else {
            mImageViewAudioOff.setSelected(true);
        }
        mImageViewAudioOff.setOnClickListener(view -> {
            if (mDirectCall != null) {
                if (mIsAudioEnabled) {
                    Log.d(TAG, "mute()");
                    mDirectCall.muteMicrophone();
                    mIsAudioEnabled = false;
                    mImageViewAudioOff.setSelected(true);
                } else {
                    Log.d(TAG, "unmute()");
                    mDirectCall.unmuteMicrophone();
                    mIsAudioEnabled = true;
                    mImageViewAudioOff.setSelected(false);
                }
            }
        });

        mImageViewEnd.setOnClickListener(view -> {
            if (mDirectCall != null) {
                end(mDirectCall);
            }
        });
    }

    protected void setListener(DirectCall call) {
        Log.d(TAG, "setListener()");

        call.setListener(new DirectCallListener() {
            @Override
            public void onConnected(DirectCall call) {
                Log.d(TAG, "onConnected()");
                setState(STATE.STATE_CONNECTED, call);
            }

            @Override
            public void onEnded(DirectCall call) {
                Log.d(TAG, "onEnded()");
                setState(STATE.STATE_ENDED, call);
            }

            @Override
            public void onRemoteVideoSettingsChanged(DirectCall call) {
                Log.d(TAG, "onRemoteVideoSettingsChanged()");
            }

            @Override
            public void onRemoteAudioSettingsChanged(DirectCall call) {
                Log.d(TAG, "onRemoteAudioSettingsChanged()");
                setRemoteMuteInfo(call);
            }

            @Override
            public void onAudioDeviceChanged(DirectCall call, AudioDevice currentAudioDevice, Set<AudioDevice> availableAudioDevices) {
                Log.d(TAG, "onAudioDeviceChanged(currentAudioDevice: " + currentAudioDevice + ", availableAudioDevices: " + availableAudioDevices + ")");
                audioDeviceChanged(call, currentAudioDevice, availableAudioDevices);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent()");

        String incomingCallId = intent.getStringExtra(ActivityUtils.EXTRA_INCOMING_CALL_ID);
        if (incomingCallId != null) {
            DirectCall call = SendBirdCall.getCall(incomingCallId);
            call.end();
        }
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    private boolean setInitialState() {
        if (mIncomingCallId != null) {
            Log.d(TAG, "setInitialState() => (mIncomingCallId != null)");

            if (mDirectCall.isEnded()) {
                Log.d(TAG, "setInitialState() => (mDirectCall.isEnded() == true)");
                setState(STATE.STATE_ENDED, mDirectCall);
                return false;
            }

            setState(STATE.STATE_INCOMING, mDirectCall);
        } else {
            setState(STATE.STATE_OUTGOING, mDirectCall);
        }
        return true;
    }

    private void checkAuthenticate() {
        if (SendBirdCall.getCurrentUser() == null)  {
            AuthenticationUtils.autoAuthenticate(mContext, userId -> {
                if (userId == null) {
                    finishWithEnding("autoAuthenticate() failed.");
                    return;
                }
                checkPermissions();
            });
        } else {
            checkPermissions();
        }
    }

    private void checkPermissions() {
        ArrayList<String> deniedPermissions = new ArrayList<>();
        for (String permission : getMandatoryPermissions()) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }

        if (deniedPermissions.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(deniedPermissions.toArray(new String[0]), REQUEST_PERMISSIONS_REQUEST_CODE);
            } else {
                finishWithEnding("Permission denied.");
            }
        } else {
            ready();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            boolean allowed = true;

            for (int result : grantResults) {
                allowed = allowed && (result == PackageManager.PERMISSION_GRANTED);
            }

            if (allowed) {
                ready();
            } else {
                finishWithEnding("Permission denied.");
            }
        }
    }

    private void ready() {
        if (mState == STATE.STATE_OUTGOING) {
            startCall(false);
        }
    }

    protected boolean setState(STATE state, DirectCall call) {
        if (isFinishing()) {
            Log.d(TAG, "setState() => isFinishing()");
            return false;
        }

        mState = state;
        switch (state) {
            case STATE_INCOMING: {
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                mLinearLayoutRemoteMute.setVisibility(View.GONE);
                mRelativeLayoutRingingButtons.setVisibility(View.VISIBLE);
                mLinearLayoutConnectingButtons.setVisibility(View.GONE);

                if (mIsVideoCall) {
                    setInfo(call, getString(R.string.calls_incoming_video_call));
                } else {
                    setInfo(call, getString(R.string.calls_incoming_voice_call));
                }

                mImageViewDecline.setBackgroundResource(R.drawable.btn_call_decline);
                break;
            }

            case STATE_ACCEPTING: {
                setInfo(call, getString(R.string.calls_connecting_call));
                break;
            }

            case STATE_OUTGOING: {
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                mImageViewProfile.setVisibility(View.GONE);
                mLinearLayoutRemoteMute.setVisibility(View.GONE);
                mRelativeLayoutRingingButtons.setVisibility(View.GONE);
                mLinearLayoutConnectingButtons.setVisibility(View.VISIBLE);

                if (mIsVideoCall) {
                    setInfo(call, getString(R.string.calls_video_calling));
                } else {
                    setInfo(call, getString(R.string.calls_calling));
                }
                break;
            }

            case STATE_CONNECTED: {
                mImageViewProfile.setVisibility(View.VISIBLE);
                mLinearLayoutRemoteMute.setVisibility(View.VISIBLE);
                mRelativeLayoutRingingButtons.setVisibility(View.GONE);
                mLinearLayoutConnectingButtons.setVisibility(View.VISIBLE);

                setRemoteMuteInfo(call);
                break;
            }

            case STATE_ENDING: {
                if (mIsVideoCall) {
                    setInfo(call, getString(R.string.calls_ending_video_call));
                } else {
                    setInfo(call, getString(R.string.calls_ending_voice_call));
                }
                break;
            }

            case STATE_ENDED: {
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                mImageViewProfile.setVisibility(View.VISIBLE);
                mLinearLayoutRemoteMute.setVisibility(View.GONE);
                mRelativeLayoutRingingButtons.setVisibility(View.GONE);
                mLinearLayoutConnectingButtons.setVisibility(View.GONE);

                String status = getEndResultString(call);
                setInfo(call, status);
                finishWithEnding(status);
                break;
            }
        }
        return true;
    }

    protected void setInfo(DirectCall call, String status) {
        DirectCallUser remoteUser = (call != null ? call.getRemoteUser() : null);
        if (remoteUser != null) {
            UserInfoUtils.setProfileImage(mContext, remoteUser, mImageViewProfile);
            UserInfoUtils.setUserId(remoteUser, mTextViewUserId);
        } else {
            mTextViewUserId.setText(mCalleeId);
        }

        mTextViewStatus.setVisibility(View.VISIBLE);
        if (status != null) {
            mTextViewStatus.setText(status);
        }
    }

    private void setRemoteMuteInfo(DirectCall call) {
        if (call != null && !call.isRemoteAudioEnabled() && call.getRemoteUser() != null) {
            String remoteUserId = call.getRemoteUser().getUserId();
            mTextViewRemoteMute.setText(getString(R.string.calls_muted_this_call, remoteUserId));
            mLinearLayoutRemoteMute.setVisibility(View.VISIBLE);
        } else {
            mLinearLayoutRemoteMute.setVisibility(View.GONE);
        }
    }

    private String getEndResultString(DirectCall call) {
        String endResultString = "";
        if (call != null) {
            switch (call.getEndResult()) {
                case NONE:
                    break;
                case NO_ANSWER:
                    endResultString = getString(R.string.calls_end_result_no_answer);
                    break;
                case CANCELED:
                    endResultString = getString(R.string.calls_end_result_canceled);
                    break;
                case DECLINED:
                    endResultString = getString(R.string.calls_end_result_declined);
                    break;
                case COMPLETED:
                    endResultString = getString(R.string.calls_end_result_completed);
                    break;
                case TIMED_OUT:
                    endResultString = getString(R.string.calls_end_result_timed_out);
                    break;
                case CONNECTION_LOST:
                    endResultString = getString(R.string.calls_end_result_connection_lost);
                    break;
                case UNKNOWN:
                    endResultString = getString(R.string.calls_end_result_unknown);
                    break;
                case DIAL_FAILED:
                    endResultString = getString(R.string.calls_end_result_dial_failed);
                    break;
                case ACCEPT_FAILED:
                    endResultString = getString(R.string.calls_end_result_accept_failed);
                    break;
                case OTHER_DEVICE_ACCEPTED:
                    endResultString = getString(R.string.calls_end_result_other_device_accepted);
                    break;
            }
        }
        return endResultString;
    }

    @Override
    public void onBackPressed() {
    }

    protected void end(DirectCall call) {
        if (call != null) {
            Log.d(TAG, "end(callId: " + call.getCallId() + ")");

            if (mState == STATE.STATE_ENDING) {
                Log.d(TAG, "Already ending call.");
                return;
            }

            setState(STATE.STATE_ENDING, call);
            call.end();
        }
    }

    protected void finishWithEnding(String log) {
        Log.d(TAG, "finishWithEnding(" + log + ")");

        if (mEndingTimer == null) {
            mEndingTimer = new Timer();
            mEndingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                runOnUiThread(() -> {
                    Log.d(TAG, "finish()");
                    finish();
                });
                }
            }, ENDING_TIME_MS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sIsRunning = false;
    }
}

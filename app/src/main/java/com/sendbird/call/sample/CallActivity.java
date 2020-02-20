package com.sendbird.call.sample;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.call.AudioDevice;
import com.sendbird.call.CallOptions;
import com.sendbird.call.DirectCall;
import com.sendbird.call.DirectCallUser;
import com.sendbird.call.SendBirdCall;
import com.sendbird.call.handler.DirectCallListener;
import com.sendbird.call.sample.utils.LoginUtils;
import com.sendbird.call.sample.utils.Utils;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class CallActivity extends AppCompatActivity {

    private static final String EXTRA_INCOMING_CALL_ID = "incoming_call_id";
    private static final String EXTRA_CALLEE_ID =        "callee_id";

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private static final String[] MANDATORY_PERMISSIONS = {
        Manifest.permission.RECORD_AUDIO,
    };

    private enum STATE {
        INCOMING_AUDIO_CALL,
        ACCEPTING_AUDIO_CALL,
        OUTGOING_AUDIO_CALL,
        CONNECTED_AUDIO_CALL,
        ENDING_AUDIO_CALL,
        CALL_ENDED,
    }

    private Context mContext;
    private String mIncomingCallId;
    private String mCalleeId;
    private DirectCall mDirectCall;

    private STATE mState;
    private boolean mAudioEnabled = true;
    private Timer mCallDurationTimer;
    private Timer mEndingTimer;

    //+ Views
    private LinearLayout mLinearLayoutInfo;
    private ImageView mImageViewProfile;
    private TextView mTextViewNicknameOrUserId;
    private TextView mTextViewStatus;

    private LinearLayout mLinearLayoutRemoteMute;
    private TextView mTextViewRemoteMute;

    private RelativeLayout mRelativeLayoutConnectingButtons;
    private ImageView mImageViewDeclineOrCancel;
    private TextView mTextViewDeclineOrCancel;
    private LinearLayout mLinearLayoutAccept;
    private ImageView mImageViewAccept;
    private TextView mTextViewAccept;

    private LinearLayout mLinearLayoutConnectedButtons;
    private ToggleButton mToggleButtonSpeakerphone;
    private ToggleButton mToggleButtonBluetooth;
    private ImageView mImageViewAudio;
    private ImageView mImageViewEnd;
    //- Views


    public static void startAsCaller(Context context, String calleeId) {
        Log.e(BaseApplication.TAG, "[CallActivity] startAsCaller()");

        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(EXTRA_CALLEE_ID, calleeId);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startAsCallee(Context context, DirectCall call) {
        Log.e(BaseApplication.TAG, "[CallActivity] startAsCallee()");

        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(CallActivity.EXTRA_INCOMING_CALL_ID, call.getCallId());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }

    private void setListener(DirectCall call) {
        Log.e(BaseApplication.TAG, "[CallActivity] setListener()");

        call.setListener(new DirectCallListener() {
            @Override
            public void onConnected(DirectCall call) {
                Log.e(BaseApplication.TAG, "[CallActivity] onConnected()");

                setState(STATE.CONNECTED_AUDIO_CALL, call);
            }

            @Override
            public void onEnded(DirectCall call) {
                Log.e(BaseApplication.TAG, "[CallActivity] onEnded()");

                setState(STATE.CALL_ENDED, call);
            }

            @Override
            public void onRemoteAudioSettingsChanged(DirectCall call) {
                Log.e(BaseApplication.TAG, "[CallActivity] onRemoteAudioSettingsChanged()");

                setRemoteMuteInfo(call);
            }

            @Override
            public void onAudioDeviceChanged(DirectCall call, AudioDevice currentAudioDevice, Set<AudioDevice> availableAudioDevices) {
                Log.e(BaseApplication.TAG, "[CallActivity] onAudioDeviceChanged(currentAudioDevice: " + currentAudioDevice + ", availableAudioDevices: " + availableAudioDevices + ")");

                Utils.showToast(mContext, "" + currentAudioDevice);

                if (currentAudioDevice == AudioDevice.SPEAKER_PHONE) {
                    mToggleButtonSpeakerphone.setChecked(true);
                    mToggleButtonBluetooth.setChecked(false);
                } else if (currentAudioDevice == AudioDevice.BLUETOOTH) {
                    mToggleButtonSpeakerphone.setChecked(false);
                    mToggleButtonBluetooth.setChecked(true);
                } else {
                    mToggleButtonSpeakerphone.setChecked(false);
                    mToggleButtonBluetooth.setChecked(false);
                }

                if (availableAudioDevices.contains(AudioDevice.SPEAKER_PHONE)) {
                    mToggleButtonSpeakerphone.setEnabled(true);
                } else if (!mToggleButtonSpeakerphone.isChecked()) {
                    mToggleButtonSpeakerphone.setEnabled(false);
                }

                if (availableAudioDevices.contains(AudioDevice.BLUETOOTH)) {
                    mToggleButtonBluetooth.setEnabled(true);
                } else if (!mToggleButtonBluetooth.isChecked()) {
                    mToggleButtonBluetooth.setEnabled(false);
                }
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(BaseApplication.TAG, "[CallActivity] onCreate()");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.activity_call);

        mContext = this;
        mIncomingCallId = getIntent().getStringExtra(EXTRA_INCOMING_CALL_ID);
        if (mIncomingCallId != null) {
            mDirectCall = SendBirdCall.getCall(mIncomingCallId);
            mCalleeId = mDirectCall.getCallee().getUserId();

            setListener(mDirectCall);
        } else {
            mCalleeId = getIntent().getStringExtra(EXTRA_CALLEE_ID);
        }

        initViews();

        if (setInitialState()) {
            checkAuthenticate();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(BaseApplication.TAG, "[CallActivity] onNewIntent()");

        String incomingCallId = intent.getStringExtra(EXTRA_INCOMING_CALL_ID);
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

    private void initViews() {
        mLinearLayoutInfo = findViewById(R.id.linear_layout_info);
        mImageViewProfile = findViewById(R.id.image_view_profile);
        mTextViewNicknameOrUserId = findViewById(R.id.text_view_nickname_or_user_id);
        mTextViewStatus = findViewById(R.id.text_view_status);

        mLinearLayoutRemoteMute = findViewById(R.id.linear_layout_remote_mute);
        mTextViewRemoteMute = findViewById(R.id.text_view_remote_mute);

        mRelativeLayoutConnectingButtons = findViewById(R.id.relative_layout_connecting_buttons);
        mImageViewDeclineOrCancel = findViewById(R.id.image_view_decline_or_cancel);
        mTextViewDeclineOrCancel = findViewById(R.id.text_view_decline_or_cancel);
        mLinearLayoutAccept = findViewById(R.id.linear_layout_accept);
        mImageViewAccept = findViewById(R.id.image_view_accept);
        mTextViewAccept = findViewById(R.id.text_view_accept);

        mLinearLayoutConnectedButtons = findViewById(R.id.linear_layout_connected_buttons);
        mToggleButtonSpeakerphone = findViewById(R.id.toggle_button_speakerphone);
        mToggleButtonBluetooth = findViewById(R.id.toggle_button_bluetooth);
        mImageViewAudio = findViewById(R.id.image_view_audio);
        mImageViewEnd = findViewById(R.id.image_view_end);

        mImageViewAccept.setOnClickListener(view -> {
            if (SendBirdCall.getCurrentUser() == null) {
                Log.e(BaseApplication.TAG, "[CallActivity] mImageViewAccept clicked => (SendBirdCall.getCurrentUser() == null)");
                return;
            }

            if (mState == STATE.ENDING_AUDIO_CALL) {
                Log.e(BaseApplication.TAG, "[CallActivity] mImageViewAccept clicked => Already ending call.");
                return;
            }

            if (mState == STATE.ACCEPTING_AUDIO_CALL) {
                Log.e(BaseApplication.TAG, "[CallActivity] mImageViewAccept clicked => Already accepting call.");
                return;
            }

            setState(STATE.ACCEPTING_AUDIO_CALL, mDirectCall);

            startCall(true);
        });

        mImageViewDeclineOrCancel.setOnClickListener(view -> {
            if (mDirectCall == null) {
                finishWithEnding("mDirectCall == null");
                return;
            }

            end(mDirectCall);
        });

        mToggleButtonSpeakerphone.setOnClickListener(view -> {
            mToggleButtonSpeakerphone.toggle();

            if (mToggleButtonSpeakerphone.isChecked()) {
                if (!mDirectCall.selectAudioDevice(AudioDevice.WIRED_HEADSET)) {
                    mDirectCall.selectAudioDevice(AudioDevice.EARPIECE);
                }
            } else {
                mDirectCall.selectAudioDevice(AudioDevice.SPEAKER_PHONE);
            }
        });

        mToggleButtonBluetooth.setOnClickListener(view -> {
            mToggleButtonBluetooth.toggle();

            if (mToggleButtonBluetooth.isChecked()) {
                if (!mDirectCall.selectAudioDevice(AudioDevice.WIRED_HEADSET)) {
                    mDirectCall.selectAudioDevice(AudioDevice.EARPIECE);
                }
            } else {
                mDirectCall.selectAudioDevice(AudioDevice.BLUETOOTH);
            }
        });

        if (mAudioEnabled) {
            mImageViewAudio.setBackgroundResource(R.drawable.ic_callkit_audio_off_white);
        } else {
            mImageViewAudio.setBackgroundResource(R.drawable.ic_callkit_audio_off_black);
        }
        mImageViewAudio.setOnClickListener(view -> {
            if (mDirectCall != null) {
                if (mAudioEnabled) {
                    Log.e(BaseApplication.TAG, "[CallActivity] mute()");
                    mDirectCall.muteMicrophone();
                    mAudioEnabled = false;
                    mImageViewAudio.setBackgroundResource(R.drawable.ic_callkit_audio_off_black);
                } else {
                    Log.e(BaseApplication.TAG, "[CallActivity] unmute()");
                    mDirectCall.unmuteMicrophone();
                    mAudioEnabled = true;
                    mImageViewAudio.setBackgroundResource(R.drawable.ic_callkit_audio_off_white);
                }
            }
        });

        mImageViewEnd.setOnClickListener(view -> {
            if (mDirectCall != null) {
                end(mDirectCall);
            }
        });
    }

    private boolean setInitialState() {
        if (mIncomingCallId != null) {
            Log.e(BaseApplication.TAG, "[CallActivity] setInitialState() => (mIncomingCallId != null)");

            if (mDirectCall.isEnded()) {
                Log.e(BaseApplication.TAG, "[CallActivity] setInitialState() => (mDirectCall.isEnded() == true)");
                setState(STATE.CALL_ENDED, mDirectCall);
                return false;
            }

            setState(STATE.INCOMING_AUDIO_CALL, mDirectCall);
        } else {
            setState(STATE.OUTGOING_AUDIO_CALL, mDirectCall);
        }
        return true;
    }

    private void checkAuthenticate() {
        if (SendBirdCall.getCurrentUser() == null)  {
            LoginUtils.autoLogin(mContext, userId -> {
                if (userId == null) {
                    finishWithEnding("AutoLogin failed.");
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
        for (String permission : MANDATORY_PERMISSIONS) {
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
        if (mState == STATE.OUTGOING_AUDIO_CALL) {
            startCall(false);
        }
    }

    private void startCall(boolean amICallee) {
        CallOptions callOptions = new CallOptions();
        callOptions.setAudioEnabled(mAudioEnabled);

        if (amICallee) {
            Log.e(BaseApplication.TAG, "[CallActivity] accept()");
            mDirectCall.accept(callOptions);
        } else {
            Log.e(BaseApplication.TAG, "[CallActivity] dial()");
            mDirectCall = SendBirdCall.dial(mCalleeId, false, callOptions, (directCall, e) -> {
                if (e != null) {
                    Log.e(BaseApplication.TAG, "[CallActivity] dial() => e: " + e.getMessage());
                    finishWithEnding(e.getMessage());
                    return;
                }

                Log.e(BaseApplication.TAG, "[CallActivity] dial() => OK");
            });

            if (mDirectCall != null) {
                setListener(mDirectCall);
            }
        }
    }

    @TargetApi(18)
    private void setState(STATE state, DirectCall call) {
        if (isFinishing()) {
            Log.e(BaseApplication.TAG, "[CallActivity] setState() => isFinishing()");
            return;
        }

        mState = state;

        if (state == STATE.INCOMING_AUDIO_CALL || state == STATE.OUTGOING_AUDIO_CALL || state == STATE.CONNECTED_AUDIO_CALL
                || (state == STATE.CALL_ENDED && call != null && !call.isVideoCall())) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        }

        switch (mState) {
            case INCOMING_AUDIO_CALL: {
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                mLinearLayoutRemoteMute.setVisibility(View.GONE);
                mRelativeLayoutConnectingButtons.setVisibility(View.VISIBLE);
                mLinearLayoutConnectedButtons.setVisibility(View.GONE);

                setInfo(call, getString(R.string.sendbirdcall_receiving_voice_call));

                mTextViewDeclineOrCancel.setText(getString(R.string.sendbirdcall_decline));
                mLinearLayoutAccept.setVisibility(View.VISIBLE);
                mImageViewAccept.setBackgroundResource(R.drawable.ic_callkit_audio);
                mTextViewAccept.setText(getString(R.string.sendbirdcall_accept));
                break;
            }

            case ACCEPTING_AUDIO_CALL: {
                setInfo(call, getString(R.string.sendbirdcall_accepting_voice_call));
                break;
            }

            case OUTGOING_AUDIO_CALL: {
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                mLinearLayoutRemoteMute.setVisibility(View.GONE);
                mRelativeLayoutConnectingButtons.setVisibility(View.VISIBLE);
                mLinearLayoutConnectedButtons.setVisibility(View.GONE);

                setInfo(call, getString(R.string.sendbirdcall_requesting_voice_call));

                mTextViewDeclineOrCancel.setText(getString(R.string.sendbirdcall_cancel));
                mLinearLayoutAccept.setVisibility(View.GONE);
                break;
            }

            case CONNECTED_AUDIO_CALL: {
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                mLinearLayoutRemoteMute.setVisibility(View.VISIBLE);
                mRelativeLayoutConnectingButtons.setVisibility(View.GONE);
                mLinearLayoutConnectedButtons.setVisibility(View.VISIBLE);

                setInfo(call, "");
                setRemoteMuteInfo(call);
                setCallDurationTimer(call);

//                LinearLayout.LayoutParams imageViewAudioLayoutParams = (LinearLayout.LayoutParams)mImageViewAudio.getLayoutParams();
//                imageViewAudioLayoutParams.rightMargin = Utils.convertDpToPixel(this, 62);
//                mImageViewAudio.setLayoutParams(imageViewAudioLayoutParams);
//
//                LinearLayout.LayoutParams imageViewEndLayoutParams = (LinearLayout.LayoutParams)mImageViewEnd.getLayoutParams();
//                imageViewEndLayoutParams.leftMargin = Utils.convertDpToPixel(this, 62);
//                mImageViewEnd.setLayoutParams(imageViewEndLayoutParams);
                break;
            }

            case ENDING_AUDIO_CALL: {
                cancelCallDurationTimer();
                setInfo(call, getString(R.string.sendbirdcall_ending_voice_call));
                break;
            }

            case CALL_ENDED: {
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                mLinearLayoutRemoteMute.setVisibility(View.GONE);
                mRelativeLayoutConnectingButtons.setVisibility(View.GONE);
                mLinearLayoutConnectedButtons.setVisibility(View.GONE);

                String status = "";
                if (call != null) {
                    switch (call.getEndResult()) {
                        case NONE:
                            break;
                        case NO_ANSWER:
                            status = getString(R.string.sendbirdcall_end_result_no_answer);
                            break;
                        case CANCELED:
                            status = getString(R.string.sendbirdcall_end_result_canceled);
                            break;
                        case DECLINED:
                            status = getString(R.string.sendbirdcall_end_result_declined);
                            break;
                        case COMPLETED:
                            status = getString(R.string.sendbirdcall_end_result_completed);
                            break;
                        case TIMED_OUT:
                            status = getString(R.string.sendbirdcall_end_result_timed_out);
                            break;
                        case CONNECTION_LOST:
                            status = getString(R.string.sendbirdcall_end_result_connection_lost);
                            break;
                        case UNKNOWN:
                            status = getString(R.string.sendbirdcall_end_result_unknown);
                            break;
                        case DIAL_FAILED:
                            status = getString(R.string.sendbirdcall_end_result_dial_failed);
                            break;
                        case ACCEPT_FAILED:
                            status = getString(R.string.sendbirdcall_end_result_accept_failed);
                            break;
                        case OTHER_DEVICE_ACCEPTED:
                            status = getString(R.string.sendbirdcall_end_result_other_device_accepted);
                            break;
                    }
                }

                cancelCallDurationTimer();
                setInfo(call, status);

                finishWithEnding(status);
                break;
            }
        }
    }

    private void setInfo(DirectCall call, String status) {
        DirectCallUser remoteUser = (call != null ? call.getRemoteUser() : null);
        if (remoteUser != null) {
            Utils.displayRoundImageFromUrl(this, remoteUser.getProfileUrl(), mImageViewProfile);

            String nickname = remoteUser.getNickname();
            if (nickname == null || nickname.length() == 0) {
                nickname = remoteUser.getUserId();
            }
            mTextViewNicknameOrUserId.setText(nickname);
        } else {
            mTextViewNicknameOrUserId.setText(mCalleeId);
        }

        mTextViewStatus.setVisibility(View.VISIBLE);

        if (status != null) {
            mTextViewStatus.setText(status);
        }
    }

    private void setRemoteMuteInfo(DirectCall call) {
        if (call != null && !call.isRemoteAudioEnabled() && call.getRemoteUser() != null) {
            String remoteNicknameOrUserId = call.getRemoteUser().getNickname();
            if (TextUtils.isEmpty(remoteNicknameOrUserId)) {
                remoteNicknameOrUserId = call.getRemoteUser().getUserId();
            }
            mTextViewRemoteMute.setText(getString(R.string.sendbirdcall_muted_this_call, remoteNicknameOrUserId));
            mLinearLayoutRemoteMute.setVisibility(View.VISIBLE);
        } else {
            mLinearLayoutRemoteMute.setVisibility(View.GONE);
        }
    }

    private void setCallDurationTimer(final DirectCall call) {
        if (mCallDurationTimer == null) {
            mCallDurationTimer = new Timer();
            mCallDurationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        String callDuration = Utils.getTimeString(call.getDuration());
                        mTextViewStatus.setText(callDuration);
                    });
                }
            }, 0, 1000);
        }
    }

    private void cancelCallDurationTimer() {
        if (mCallDurationTimer != null) {
            mCallDurationTimer.cancel();
            mCallDurationTimer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(BaseApplication.TAG, "[CallActivity] onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(BaseApplication.TAG, "[CallActivity] onPause()");
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(BaseApplication.TAG, "[CallActivity] onDestroy()");

        cancelCallDurationTimer();
    }

    private void end(DirectCall call) {
        if (call != null) {
            Log.e(BaseApplication.TAG, "[CallActivity] end(callId: " + call.getCallId() + ")");

            if (mState == STATE.ENDING_AUDIO_CALL) {
                Log.e(BaseApplication.TAG, "[CallActivity] Already ending call.");
                return;
            }

            setState(STATE.ENDING_AUDIO_CALL, call);
            call.end();
        }
    }

    private void finishWithEnding(String log) {
        Log.e(BaseApplication.TAG, "[CallActivity] finishWithEnding(" + log + ")");

        if (mEndingTimer == null) {
            mEndingTimer = new Timer();
            mEndingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                runOnUiThread(() -> {
                    Log.e(BaseApplication.TAG, "[CallActivity] finish()");
                    finish();
                });
                }
            }, 1000);
        }
    }
}

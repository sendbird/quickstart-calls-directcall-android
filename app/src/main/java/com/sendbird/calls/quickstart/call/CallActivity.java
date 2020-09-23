package com.sendbird.calls.quickstart.call;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sendbird.calls.AudioDevice;
import com.sendbird.calls.DirectCall;
import com.sendbird.calls.DirectCallUser;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.handler.DirectCallListener;
import com.sendbird.calls.quickstart.BaseApplication;
import com.sendbird.calls.quickstart.R;
import com.sendbird.calls.quickstart.utils.AuthenticationUtils;
import com.sendbird.calls.quickstart.utils.BroadcastUtils;
import com.sendbird.calls.quickstart.utils.EndResultUtils;
import com.sendbird.calls.quickstart.utils.UserInfoUtils;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public abstract class CallActivity extends AppCompatActivity {

    static final int ENDING_TIME_MS = 1000;

    public enum STATE {
        STATE_ACCEPTING,
        STATE_OUTGOING,
        STATE_CONNECTED,
        STATE_ENDING,
        STATE_ENDED
    }

    Context mContext;

    STATE mState;
    private String mCallId;
    boolean mIsVideoCall;
    String mCalleeIdToDial;
    private boolean mDoDial;
    private boolean mDoAccept;
    protected boolean mDoLocalVideoStart;

    private boolean mDoEnd;

    DirectCall mDirectCall;
    boolean mIsAudioEnabled;
    private Timer mEndingTimer;

    //+ Views
    LinearLayout mLinearLayoutInfo;
    ImageView mImageViewProfile;
    TextView mTextViewUserId;
    TextView mTextViewStatus;

    LinearLayout mLinearLayoutRemoteMute;
    TextView mTextViewRemoteMute;

    RelativeLayout mRelativeLayoutRingingButtons;
    ImageView mImageViewDecline;

    LinearLayout mLinearLayoutConnectingButtons;
    ImageView mImageViewAudioOff;
    ImageView mImageViewBluetooth;
    ImageView mImageViewEnd;
    //- Views

    //+ abstract methods
    protected abstract int getLayoutResourceId();
    protected abstract void setAudioDevice(AudioDevice currentAudioDevice, Set<AudioDevice> availableAudioDevices);
    protected abstract void startCall(boolean amICallee);
    //- abstract methods

    //+ CallService
    private CallService mCallService;
    private boolean mBound = false;
    //- CallService


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(BaseApplication.TAG, "[CallActivity] onCreate()");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(getLayoutResourceId());

        mContext = this;

        bindCallService();

        init();
        initViews();
        setViews();
        setAudioDevice();
        setCurrentState();

        if (mDoEnd) {
            Log.i(BaseApplication.TAG, "[CallActivity] init() => (mDoEnd == true)");
            end();
            return;
        }

        checkAuthentication();
    }

    private void init() {
        Intent intent = getIntent();

        mState = (STATE) intent.getSerializableExtra(CallService.EXTRA_CALL_STATE);
        mCallId = intent.getStringExtra(CallService.EXTRA_CALL_ID);
        mIsVideoCall = intent.getBooleanExtra(CallService.EXTRA_IS_VIDEO_CALL, false);
        mCalleeIdToDial = intent.getStringExtra(CallService.EXTRA_CALLEE_ID_TO_DIAL);
        mDoDial = intent.getBooleanExtra(CallService.EXTRA_DO_DIAL, false);
        mDoAccept = intent.getBooleanExtra(CallService.EXTRA_DO_ACCEPT, false);
        mDoLocalVideoStart = intent.getBooleanExtra(CallService.EXTRA_DO_LOCAL_VIDEO_START, false);

        mDoEnd = intent.getBooleanExtra(CallService.EXTRA_DO_END, false);

        Log.i(BaseApplication.TAG, "[CallActivity] init() => (mState: " + mState + ", mCallId: " + mCallId + ", mIsVideoCall: " + mIsVideoCall
                + ", mCalleeIdToDial: " + mCalleeIdToDial + ", mDoDial: " + mDoDial + ", mDoAccept: " + mDoAccept + ", mDoLocalVideoStart: " + mDoLocalVideoStart
                + ", mDoEnd: " + mDoEnd + ")");

        if (mCallId != null) {
            mDirectCall = SendBirdCall.getCall(mCallId);
            setListener(mDirectCall);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(BaseApplication.TAG, "[CallActivity] onNewIntent()");

        mDoEnd = intent.getBooleanExtra(CallService.EXTRA_DO_END, false);
        if (mDoEnd) {
            Log.i(BaseApplication.TAG, "[CallActivity] onNewIntent() => (mDoEnd == true)");
            end();
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

        mLinearLayoutConnectingButtons = findViewById(R.id.linear_layout_connecting_buttons);
        mImageViewAudioOff = findViewById(R.id.image_view_audio_off);
        mImageViewBluetooth = findViewById(R.id.image_view_bluetooth);
        mImageViewEnd = findViewById(R.id.image_view_end);
    }

    protected void setViews() {
        mImageViewDecline.setOnClickListener(view -> {
            end();
        });

        if (mDirectCall != null) {
            mIsAudioEnabled = mDirectCall.isLocalAudioEnabled();
        } else {
            mIsAudioEnabled = true;
        }
        if (mIsAudioEnabled) {
            mImageViewAudioOff.setSelected(false);
        } else {
            mImageViewAudioOff.setSelected(true);
        }
        mImageViewAudioOff.setOnClickListener(view -> {
            if (mDirectCall != null) {
                if (mIsAudioEnabled) {
                    Log.i(BaseApplication.TAG, "[CallActivity] mute()");
                    mDirectCall.muteMicrophone();
                    mIsAudioEnabled = false;
                    mImageViewAudioOff.setSelected(true);
                } else {
                    Log.i(BaseApplication.TAG, "[CallActivity] unmute()");
                    mDirectCall.unmuteMicrophone();
                    mIsAudioEnabled = true;
                    mImageViewAudioOff.setSelected(false);
                }
            }
        });

        mImageViewEnd.setOnClickListener(view -> {
            end();
        });
    }

    private void setAudioDevice() {
        if (mDirectCall != null) {
            setAudioDevice(mDirectCall.getCurrentAudioDevice(), mDirectCall.getAvailableAudioDevices());
        }
    }

    private void setCurrentState() {
        setState(mState, mDirectCall);
    }

    protected void setListener(DirectCall call) {
        Log.i(BaseApplication.TAG, "[CallActivity] setListener()");

        if (call != null) {
            call.setListener(new DirectCallListener() {
                @Override
                public void onConnected(DirectCall call) {
                    Log.i(BaseApplication.TAG, "[CallActivity] onConnected()");
                    setState(STATE.STATE_CONNECTED, call);
                }

                @Override
                public void onEnded(DirectCall call) {
                    Log.i(BaseApplication.TAG, "[CallActivity] onEnded()");
                    setState(STATE.STATE_ENDED, call);

                    BroadcastUtils.sendCallLogBroadcast(mContext, call.getCallLog());
                }

                @Override
                public void onRemoteVideoSettingsChanged(DirectCall call) {
                    Log.i(BaseApplication.TAG, "[CallActivity] onRemoteVideoSettingsChanged()");
                }

                @Override
                public void onLocalVideoSettingsChanged(DirectCall call) {
                    Log.i(BaseApplication.TAG, "[CallActivity] onLocalVideoSettingsChanged()");
                    if (CallActivity.this instanceof VideoCallActivity) {
                        ((VideoCallActivity) CallActivity.this).setLocalVideoSettings(call);
                    }
                }

                @Override
                public void onRemoteAudioSettingsChanged(DirectCall call) {
                    Log.i(BaseApplication.TAG, "[CallActivity] onRemoteAudioSettingsChanged()");
                    setRemoteMuteInfo(call);
                }

                @Override
                public void onAudioDeviceChanged(DirectCall call, AudioDevice currentAudioDevice, Set<AudioDevice> availableAudioDevices) {
                    Log.i(BaseApplication.TAG, "[CallActivity] onAudioDeviceChanged(currentAudioDevice: " + currentAudioDevice + ", availableAudioDevices: " + availableAudioDevices + ")");
                    setAudioDevice(currentAudioDevice, availableAudioDevices);
                }
            });
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

    private void checkAuthentication() {
        if (SendBirdCall.getCurrentUser() == null)  {
            AuthenticationUtils.autoAuthenticate(mContext, userId -> {
                if (userId == null) {
                    finishWithEnding("autoAuthenticate() failed.");
                    return;
                }
                ready();
            });
        } else {
            ready();
        }
    }

    private void ready() {
        if (mDoDial) {
            mDoDial = false;
            startCall(false);
        } else if (mDoAccept) {
            mDoAccept = false;
            startCall(true);
        }
    }

    protected boolean setState(STATE state, DirectCall call) {
        mState = state;
        updateCallService();

        switch (state) {
            case STATE_ACCEPTING: {
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
                mLinearLayoutInfo.setVisibility(View.VISIBLE);
                mImageViewProfile.setVisibility(View.VISIBLE);
                mLinearLayoutRemoteMute.setVisibility(View.GONE);
                mRelativeLayoutRingingButtons.setVisibility(View.GONE);
                mLinearLayoutConnectingButtons.setVisibility(View.GONE);

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

                String status = "";
                if (call != null) {
                    status = EndResultUtils.getEndResultString(mContext, call.getEndResult());
                }
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
        }

        mTextViewUserId.setText(getRemoteNicknameOrUserId(call));
        mTextViewStatus.setVisibility(View.VISIBLE);
        if (status != null) {
            mTextViewStatus.setText(status);
        }
    }

    private String getRemoteNicknameOrUserId(DirectCall call) {
        String remoteNicknameOrUserId = mCalleeIdToDial;
        if (call != null) {
            remoteNicknameOrUserId = UserInfoUtils.getNicknameOrUserId(call.getRemoteUser());
        }
        return remoteNicknameOrUserId;
    }

    private void setRemoteMuteInfo(DirectCall call) {
        if (call != null && !call.isRemoteAudioEnabled() && call.getRemoteUser() != null) {
            mTextViewRemoteMute.setText(getString(R.string.calls_muted_this_call, UserInfoUtils.getNicknameOrUserId(call.getRemoteUser())));
            mLinearLayoutRemoteMute.setVisibility(View.VISIBLE);
        } else {
            mLinearLayoutRemoteMute.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
    }

    private void end() {
        if (mDirectCall != null) {
            Log.i(BaseApplication.TAG, "[CallActivity] end()");

            if (mState == STATE.STATE_ENDING || mState == STATE.STATE_ENDED) {
                Log.i(BaseApplication.TAG, "[CallActivity] Already ending call.");
                return;
            }

            if (mDirectCall.isEnded()) {
                setState(STATE.STATE_ENDED, mDirectCall);
            } else {
                setState(STATE.STATE_ENDING, mDirectCall);
                mDirectCall.end();
            }
        } else {
            Log.i(BaseApplication.TAG, "[CallActivity] end() => (mDirectCall == null)");
            finishWithEnding("(mDirectCall == null)");
        }
    }

    protected void finishWithEnding(String log) {
        Log.i(BaseApplication.TAG, "[CallActivity] finishWithEnding(" + log + ")");

        if (mEndingTimer == null) {
            mEndingTimer = new Timer();
            mEndingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                runOnUiThread(() -> {
                    Log.i(BaseApplication.TAG, "[CallActivity] finish()");
                    finish();

                    unbindCallService();
                    stopCallService();
                });
                }
            }, ENDING_TIME_MS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(BaseApplication.TAG, "[CallActivity] onDestroy()");

        unbindCallService();
    }

    //+ CallService
    private ServiceConnection mCallServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(BaseApplication.TAG, "[CallActivity] onServiceConnected()");

            CallService.CallBinder callBinder = (CallService.CallBinder) iBinder;
            mCallService = callBinder.getService();
            mBound = true;

            updateCallService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(BaseApplication.TAG, "[CallActivity] onServiceDisconnected()");

            mBound = false;
        }
    };

    private void bindCallService() {
        Log.i(BaseApplication.TAG, "[CallActivity] bindCallService()");

        bindService(new Intent(this, CallService.class), mCallServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindCallService() {
        Log.i(BaseApplication.TAG, "[CallActivity] unbindCallService()");

        if (mBound) {
            unbindService(mCallServiceConnection);
            mBound = false;
        }
    }

    private void stopCallService() {
        Log.i(BaseApplication.TAG, "[CallActivity] stopCallService()");

        CallService.stopService(mContext);
    }

    protected void updateCallService() {
        if (mCallService != null) {
            Log.i(BaseApplication.TAG, "[CallActivity] updateCallService()");

            CallService.ServiceData serviceData = new CallService.ServiceData();
            serviceData.isHeadsUpNotification = false;
            serviceData.remoteNicknameOrUserId = getRemoteNicknameOrUserId(mDirectCall);
            serviceData.callState = mState;
            serviceData.callId = (mDirectCall != null ? mDirectCall.getCallId() : mCallId);
            serviceData.isVideoCall = mIsVideoCall;
            serviceData.calleeIdToDial = mCalleeIdToDial;
            serviceData.doDial = mDoDial;
            serviceData.doAccept = mDoAccept;
            serviceData.doLocalVideoStart = mDoLocalVideoStart;

            mCallService.updateNotification(serviceData);
        }
    }
    //- CallService
}

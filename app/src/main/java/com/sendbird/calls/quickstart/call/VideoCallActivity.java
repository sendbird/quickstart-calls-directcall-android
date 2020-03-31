package com.sendbird.calls.quickstart.call;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sendbird.calls.AcceptParams;
import com.sendbird.calls.AudioDevice;
import com.sendbird.calls.CallOptions;
import com.sendbird.calls.DialParams;
import com.sendbird.calls.DirectCall;
import com.sendbird.calls.DirectCallUserRole;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.SendBirdVideoView;
import com.sendbird.calls.quickstart.R;
import com.sendbird.calls.quickstart.utils.ToastUtils;

import org.webrtc.RendererCommon;

import java.util.Set;

public class VideoCallActivity extends CallActivity {

    private static final String TAG = "VideoCallActivity";

    private static final String[] MANDATORY_PERMISSIONS = {
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    };

    private boolean mIsVideoEnabled = true;
    private boolean mIsMyVideoStopped;

    //+ Views
    private SendBirdVideoView mVideoViewFullScreen;
    private View mViewConnectingVideoViewFullScreenFg;
    private RelativeLayout mRelativeLayoutVideoViewSmall;
    private SendBirdVideoView mVideoViewSmall;
    private ImageView mImageViewCameraSwitch;
    private ImageView mImageViewVideoOff;
    //- Views

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_video_call;
    }

    @Override
    protected String[] getMandatoryPermissions() {
        return MANDATORY_PERMISSIONS;
    }

    @Override
    protected void initViews() {
        super.initViews();
        Log.d(TAG, "initViews()");

        mVideoViewFullScreen = findViewById(R.id.video_view_fullscreen);
        mViewConnectingVideoViewFullScreenFg = findViewById(R.id.view_connecting_video_view_fullscreen_fg);
        mRelativeLayoutVideoViewSmall = findViewById(R.id.relative_layout_video_view_small);
        mVideoViewSmall = findViewById(R.id.video_view_small);
        mImageViewCameraSwitch = findViewById(R.id.image_view_camera_switch);
        mImageViewVideoOff = findViewById(R.id.image_view_video_off);
    }

    @Override
    protected void setViews() {
        super.setViews();

        mImageViewCameraSwitch.setOnClickListener(view -> {
            if (mDirectCall != null) {
                mDirectCall.switchCamera(e -> {
                    if (e != null) {
                        Log.d(TAG, "switchCamera(e: " + e.getMessage() + ")");
                    }
                });
            }
        });

        mImageViewVideoOff.setSelected(!mIsVideoEnabled);
        mImageViewVideoOff.setOnClickListener(view -> {
            if (mDirectCall != null) {
                if (mIsVideoEnabled) {
                    Log.d(TAG, "stopVideo()");
                    mDirectCall.stopVideo();
                    mIsVideoEnabled = false;
                    mImageViewVideoOff.setSelected(true);
                } else {
                    Log.d(TAG, "startVideo()");
                    mDirectCall.startVideo();
                    mIsVideoEnabled = true;
                    mImageViewVideoOff.setSelected(false);
                }
            }
        });

        mImageViewBluetooth.setEnabled(false);
        mImageViewBluetooth.setOnClickListener(view -> {
            mImageViewBluetooth.setSelected(!mImageViewBluetooth.isSelected());
            if (mDirectCall != null) {
                if (mImageViewBluetooth.isSelected()) {
                    mDirectCall.selectAudioDevice(AudioDevice.BLUETOOTH, e -> {
                        if (e != null) {
                            mImageViewBluetooth.setSelected(false);
                        }
                    });
                } else {
                    mDirectCall.selectAudioDevice(AudioDevice.WIRED_HEADSET, e -> {
                        if (e != null) {
                            mDirectCall.selectAudioDevice(AudioDevice.SPEAKERPHONE, null);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void audioDeviceChanged(DirectCall call, AudioDevice currentAudioDevice, Set<AudioDevice> availableAudioDevices) {
        if (currentAudioDevice == AudioDevice.SPEAKERPHONE) {
            mImageViewBluetooth.setSelected(false);
        } else if (currentAudioDevice == AudioDevice.BLUETOOTH) {
            mImageViewBluetooth.setSelected(true);
        }

        if (availableAudioDevices.contains(AudioDevice.BLUETOOTH)) {
            mImageViewBluetooth.setEnabled(true);
        } else if (!mImageViewBluetooth.isSelected()) {
            mImageViewBluetooth.setEnabled(false);
        }
    }

    @Override
    protected void startCall(boolean amICallee) {
        CallOptions callOptions = new CallOptions();
        callOptions.setAudioEnabled(mIsAudioEnabled);

        mVideoViewFullScreen.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        mVideoViewFullScreen.setZOrderMediaOverlay(false);
        mVideoViewFullScreen.setEnableHardwareScaler(true);

        mVideoViewSmall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        mVideoViewSmall.setZOrderMediaOverlay(true);
        mVideoViewSmall.setEnableHardwareScaler(true);

        callOptions.setVideoEnabled(mIsVideoEnabled).setAudioEnabled(mIsAudioEnabled);

        if (amICallee) {
            callOptions.setLocalVideoView(mVideoViewSmall).setRemoteVideoView(mVideoViewFullScreen);
        } else {
            callOptions.setLocalVideoView(mVideoViewFullScreen).setRemoteVideoView(mVideoViewSmall);
        }

        if (amICallee) {
            Log.d(TAG, "accept()");
            if (mDirectCall != null) {
                mDirectCall.accept(new AcceptParams().setCallOptions(callOptions));
            }
        } else {
            Log.d(TAG, "dial()");
            mDirectCall = SendBirdCall.dial(new DialParams(mCalleeId).setVideoCall(mIsVideoCall).setCallOptions(callOptions), (call, e) -> {
                if (e != null) {
                    Log.d(TAG, "dial() => e: " + e.getMessage());
                    if (e.getMessage() != null) {
                        ToastUtils.showToast(mContext, e.getMessage());
                    }

                    finishWithEnding(e.getMessage());
                    return;
                }
                Log.d(TAG, "dial() => OK");
            });

            if (mDirectCall != null) {
                setListener(mDirectCall);
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @TargetApi(18)
    @Override
    protected boolean setState(STATE state, DirectCall call) {
        if (!super.setState(state,call)) {
            return false;
        }

        switch (state) {
            case STATE_INCOMING: {
                mVideoViewFullScreen.setVisibility(View.GONE);
                mViewConnectingVideoViewFullScreenFg.setVisibility(View.GONE);
                mRelativeLayoutVideoViewSmall.setVisibility(View.GONE);
                mImageViewCameraSwitch.setVisibility(View.GONE);
                break;
            }

            case STATE_OUTGOING: {
                mVideoViewFullScreen.setVisibility(View.VISIBLE);
                mViewConnectingVideoViewFullScreenFg.setVisibility(View.VISIBLE);
                mRelativeLayoutVideoViewSmall.setVisibility(View.GONE);
                mImageViewCameraSwitch.setVisibility(View.VISIBLE);
                mImageViewVideoOff.setVisibility(View.VISIBLE);
                break;
            }

            case STATE_CONNECTED: {
                mVideoViewFullScreen.setVisibility(View.VISIBLE);
                mViewConnectingVideoViewFullScreenFg.setVisibility(View.GONE);
                mRelativeLayoutVideoViewSmall.setVisibility(View.VISIBLE);
                mImageViewCameraSwitch.setVisibility(View.VISIBLE);
                mImageViewVideoOff.setVisibility(View.VISIBLE);

                mLinearLayoutInfo.setVisibility(View.GONE);

                if (call != null && call.getMyRole() == DirectCallUserRole.CALLER) {
                    call.setRemoteVideoView(mVideoViewFullScreen);
                    call.setLocalVideoView(mVideoViewSmall);
                }
                break;
            }

            case STATE_ENDED: {
                mVideoViewFullScreen.setVisibility(View.GONE);
                mViewConnectingVideoViewFullScreenFg.setVisibility(View.GONE);
                mRelativeLayoutVideoViewSmall.setVisibility(View.GONE);
                mImageViewCameraSwitch.setVisibility(View.GONE);
            }
            break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        if (mDirectCall != null && mIsMyVideoStopped) {
            mIsMyVideoStopped = false;
            mDirectCall.startVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");

        if (mDirectCall != null && mDirectCall.isLocalVideoEnabled()) {
            mDirectCall.stopVideo();
            mIsMyVideoStopped = true;
        }
    }
}

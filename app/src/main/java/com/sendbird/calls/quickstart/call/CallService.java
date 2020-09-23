package com.sendbird.calls.quickstart.call;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sendbird.calls.DirectCall;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.quickstart.BaseApplication;
import com.sendbird.calls.quickstart.R;
import com.sendbird.calls.quickstart.utils.ToastUtils;
import com.sendbird.calls.quickstart.utils.UserInfoUtils;

public class CallService extends Service {

    private static final int NOTIFICATION_ID = 1;

    public static final String EXTRA_IS_HEADS_UP_NOTIFICATION   = "is_heads_up_notification";
    public static final String EXTRA_REMOTE_NICKNAME_OR_USER_ID = "remote_nickname_or_user_id";
    public static final String EXTRA_CALL_STATE                 = "call_state";
    public static final String EXTRA_CALL_ID                    = "call_id";
    public static final String EXTRA_IS_VIDEO_CALL              = "is_video_call";
    public static final String EXTRA_CALLEE_ID_TO_DIAL          = "callee_id_to_dial";
    public static final String EXTRA_DO_DIAL                    = "do_dial";
    public static final String EXTRA_DO_ACCEPT                  = "do_accept";
    public static final String EXTRA_DO_LOCAL_VIDEO_START       = "do_local_video_start";

    public static final String EXTRA_DO_END                     = "do_end";


    private Context mContext;
    private final IBinder mBinder = new CallBinder();
    private final ServiceData mServiceData = new ServiceData();

    class CallBinder extends Binder {
        CallService getService() {
            return CallService.this;
        }
    }

    static class ServiceData {
        boolean isHeadsUpNotification;
        String remoteNicknameOrUserId;
        CallActivity.STATE callState;
        String callId;
        boolean isVideoCall;
        String calleeIdToDial;
        boolean doDial;
        boolean doAccept;
        boolean doLocalVideoStart;

        ServiceData() {
        }

        void set(ServiceData serviceData) {
            this.isHeadsUpNotification = serviceData.isHeadsUpNotification;
            this.remoteNicknameOrUserId = serviceData.remoteNicknameOrUserId;
            this.callState = serviceData.callState;
            this.callId = serviceData.callId;
            this.isVideoCall = serviceData.isVideoCall;
            this.calleeIdToDial = serviceData.calleeIdToDial;
            this.doDial = serviceData.doDial;
            this.doAccept = serviceData.doAccept;
            this.doLocalVideoStart = serviceData.doLocalVideoStart;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(BaseApplication.TAG, "[CallService] onBind()");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(BaseApplication.TAG, "[CallService] onCreate()");

        mContext = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(BaseApplication.TAG, "[CallService] onDestroy()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(BaseApplication.TAG, "[CallService] onStartCommand()");

        mServiceData.isHeadsUpNotification = intent.getBooleanExtra(EXTRA_IS_HEADS_UP_NOTIFICATION, false);
        mServiceData.remoteNicknameOrUserId = intent.getStringExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID);
        mServiceData.callState = (CallActivity.STATE) intent.getSerializableExtra(EXTRA_CALL_STATE);
        mServiceData.callId = intent.getStringExtra(EXTRA_CALL_ID);
        mServiceData.isVideoCall = intent.getBooleanExtra(EXTRA_IS_VIDEO_CALL, false);
        mServiceData.calleeIdToDial = intent.getStringExtra(EXTRA_CALLEE_ID_TO_DIAL);
        mServiceData.doDial = intent.getBooleanExtra(EXTRA_DO_DIAL, false);
        mServiceData.doAccept = intent.getBooleanExtra(EXTRA_DO_ACCEPT, false);
        mServiceData.doLocalVideoStart = intent.getBooleanExtra(EXTRA_DO_LOCAL_VIDEO_START, false);

        updateNotification(mServiceData);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(BaseApplication.TAG, "[CallService] onTaskRemoved()");

        mServiceData.isHeadsUpNotification = true;
        updateNotification(mServiceData);
    }

    private static Intent getCallActivityIntent(Context context, ServiceData serviceData, boolean doEnd) {
        final Intent intent;
        if (serviceData.isVideoCall) {
            intent = new Intent(context, VideoCallActivity.class);
        } else {
            intent = new Intent(context, VoiceCallActivity.class);
        }

        intent.putExtra(EXTRA_CALL_STATE, serviceData.callState);
        intent.putExtra(EXTRA_CALL_ID, serviceData.callId);
        intent.putExtra(EXTRA_IS_VIDEO_CALL, serviceData.isVideoCall);
        intent.putExtra(EXTRA_CALLEE_ID_TO_DIAL, serviceData.calleeIdToDial);
        intent.putExtra(EXTRA_DO_DIAL, serviceData.doDial);
        intent.putExtra(EXTRA_DO_ACCEPT, serviceData.doAccept);
        intent.putExtra(EXTRA_DO_LOCAL_VIDEO_START, serviceData.doLocalVideoStart);

        intent.putExtra(EXTRA_DO_END, doEnd);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }

    private Notification getNotification(@NonNull ServiceData serviceData) {
        final String content;
        if (serviceData.isVideoCall) {
            content = mContext.getString(R.string.calls_notification_video_calling_content, mContext.getString(R.string.calls_app_name));
        } else {
            content = mContext.getString(R.string.calls_notification_voice_calling_content, mContext.getString(R.string.calls_app_name));
        }

        final int currentTime = (int)System.currentTimeMillis();
        final String channelId = mContext.getPackageName() + currentTime;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = mContext.getString(R.string.calls_app_name);
            NotificationChannel channel = new NotificationChannel(channelId, channelName,
                    serviceData.isHeadsUpNotification ? NotificationManager.IMPORTANCE_HIGH : NotificationManager.IMPORTANCE_LOW);

            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent callIntent = getCallActivityIntent(mContext, serviceData, false);
        PendingIntent callPendingIntent = PendingIntent.getActivity(mContext, (currentTime + 1), callIntent, 0);

        Intent endIntent = getCallActivityIntent(mContext, serviceData, true);
        PendingIntent endPendingIntent = PendingIntent.getActivity(mContext, (currentTime + 2), endIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId);
        builder.setContentTitle(serviceData.remoteNicknameOrUserId)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_sendbird)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_push_oreo))
                .setPriority(serviceData.isHeadsUpNotification ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_LOW);

        if (SendBirdCall.getOngoingCallCount() > 0) {
            if (serviceData.doAccept) {
                builder.addAction(new NotificationCompat.Action(0, mContext.getString(R.string.calls_notification_decline), endPendingIntent));
                builder.addAction(new NotificationCompat.Action(0, mContext.getString(R.string.calls_notification_accept), callPendingIntent));
            } else {
                builder.setContentIntent(callPendingIntent);
                builder.addAction(new NotificationCompat.Action(0, mContext.getString(R.string.calls_notification_end), endPendingIntent));
            }
        }
        return builder.build();
    }

    public static void dial(Context context, String doDialWithCalleeId, boolean isVideoCall) {
        if (SendBirdCall.getOngoingCallCount() > 0) {
            ToastUtils.showToast(context, "Ringing.");
            Log.i(BaseApplication.TAG, "[CallService] dial() => SendBirdCall.getOngoingCallCount(): " + SendBirdCall.getOngoingCallCount());
            return;
        }

        Log.i(BaseApplication.TAG, "[CallService] dial()");

        ServiceData serviceData = new ServiceData();
        serviceData.isHeadsUpNotification = false;
        serviceData.remoteNicknameOrUserId = doDialWithCalleeId;
        serviceData.callState = CallActivity.STATE.STATE_OUTGOING;
        serviceData.callId = null;
        serviceData.isVideoCall = isVideoCall;
        serviceData.calleeIdToDial = doDialWithCalleeId;
        serviceData.doDial = true;
        serviceData.doAccept = false;
        serviceData.doLocalVideoStart = false;

        startService(context, serviceData);

        context.startActivity(getCallActivityIntent(context, serviceData, false));
    }

    public static void onRinging(Context context, @NonNull DirectCall call) {
        Log.i(BaseApplication.TAG, "[CallService] onRinging()");

        ServiceData serviceData = new ServiceData();
        serviceData.isHeadsUpNotification = true;
        serviceData.remoteNicknameOrUserId = UserInfoUtils.getNicknameOrUserId(call.getRemoteUser());
        serviceData.callState = CallActivity.STATE.STATE_ACCEPTING;
        serviceData.callId = call.getCallId();
        serviceData.isVideoCall = call.isVideoCall();
        serviceData.calleeIdToDial = null;
        serviceData.doDial = false;
        serviceData.doAccept = true;
        serviceData.doLocalVideoStart = false;

        startService(context, serviceData);
    }

    private static void startService(Context context, ServiceData serviceData) {
        Log.i(BaseApplication.TAG, "[CallService] startService()");

        if (context != null) {
            Intent intent = new Intent(context, CallService.class);

            intent.putExtra(EXTRA_IS_HEADS_UP_NOTIFICATION, serviceData.isHeadsUpNotification);
            intent.putExtra(EXTRA_REMOTE_NICKNAME_OR_USER_ID, serviceData.remoteNicknameOrUserId);
            intent.putExtra(EXTRA_CALL_STATE, serviceData.callState);
            intent.putExtra(EXTRA_CALL_ID, serviceData.callId);
            intent.putExtra(EXTRA_IS_VIDEO_CALL, serviceData.isVideoCall);
            intent.putExtra(EXTRA_CALLEE_ID_TO_DIAL, serviceData.calleeIdToDial);
            intent.putExtra(EXTRA_DO_DIAL, serviceData.doDial);
            intent.putExtra(EXTRA_DO_ACCEPT, serviceData.doAccept);
            intent.putExtra(EXTRA_DO_LOCAL_VIDEO_START, serviceData.doLocalVideoStart);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }

    public static void stopService(Context context) {
        Log.i(BaseApplication.TAG, "[CallService] stopService()");

        if (context != null) {
            Intent intent = new Intent(context, CallService.class);
            context.stopService(intent);
        }
    }

    public void updateNotification(@NonNull ServiceData serviceData) {
        Log.i(BaseApplication.TAG, "[CallService] updateNotification(isHeadsUpNotification: " + serviceData.isHeadsUpNotification + ", remoteNicknameOrUserId: " + serviceData.remoteNicknameOrUserId
                + ", callState: " + serviceData.callState + ", callId: " + serviceData.callId + ", isVideoCall: " + serviceData.isVideoCall
                + ", calleeIdToDial: " + serviceData.calleeIdToDial + ", doDial: " + serviceData.doDial + ", doAccept: " + serviceData.doAccept + ", doLocalVideoStart: " + serviceData.doLocalVideoStart + ")");

        mServiceData.set(serviceData);
        startForeground(NOTIFICATION_ID, getNotification(mServiceData));
    }
}

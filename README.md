# SendBird Calls QuickStart Guide for Android

## Introduction

The `SendBirdCalls` framework enables realtime VoIP communication between app users.  This repository contains a sample application intended to demonstrate a simple implementation of this framework. This readme document details how to get up and running using this sample application.

> If you need any helps or have any issue / question, please visit [our community](https://community.sendbird.com)

## Prerequisites

- Android 4.1 (API level 16) or later

## Creating a SendBird application

 1. Login or Sign-up for an account at [dashboard](https://dashboard.sendbird.com).
 2. Create or select an application on the SendBird Dashboard.
 3. Note the `Application ID` for future reference.
 4. [Contact sales](https://sendbird.com/contact-sales) to get the `Calls` menu enabled in the dashboard. (Self-serve coming soon.)

## Creating test users

 1. In the SendBird dashboard, navigate to the `Users` menu.
 2. Create at least two new users, one that will be the `caller`, and one that will be the `callee`.
 3. Note the `User ID` of each user for future reference.

## Specifying the Application ID

To connect this sample Android application to the SendBird application specified in the previous step, the **Application ID** of the SendBird application must be specified inside the sample Android application’s source code.
​
Replace `YOUR_APPLICATION_ID` with the `Application ID` of the SendBird application created previously.
​
```java
public class BaseApplication extends Application {
    ...
    private static final String APP_ID = "YOUR_APPLICATION_ID";
    ...
}
```

## Build install and run

 - Build and run the application. Refer to [Android Documentation](https://developer.android.com/studio/run).
 - The application must be installed onto two separate devices. (one `caller` and one `callee`)
 - If two test devices are not available, the application can be run on an emulator.

## Making calls

 1. Log in to the primary device’s sample application with the ID of the user designated as the `caller`.
 2. Log in to the secondary device’s sample application with ID of the user designated as the `callee`.  Alternatively, use the Calls widget found on the Calls dashboard to login as the `callee`.
 3. On the primary device, specify the user ID of the `callee` and initiate a call.
 4. If all steps have been followed correctly, an incoming call notification will appear on the `callee` user’s device.
 5. Reverse roles, and initiate a call from the other device.
 6. If the `caller` and `callee` devices are near each other, use headphones to prevent audio feedback.
 7. The SendBird Calls Android Sample has been successfully implemented.

## Creating a local video view before accepting incoming calls

You can create how the current user’s local video view will show on the screen before accepting an incoming call. Customize the current user’s local video view by following the steps below:

1. Start your call activity with an incoming call ID in the `onRinging()` method within the class where you implement code for receiving a call.
2. Get the `DirectCall` object with the incoming call ID within the `onCreate()` method of the call activity class.
3. Get the `SendBirdVideoView` object from the xml file of your call activity to add a local video view.
4. Call the `DirectCall.setLocalVideoView()` method by using the `SendBirdVideoView` object within the call activity class.

```java
// {YourApplication}.java
SendBirdCall.addListener(UUID.randomUUID().toString(), new SendBirdCallListener() {
    @Override
    public void onRinging(DirectCall call) {
        ...
        Intent intent = new Intent(context, YourCallActivity.class);
        intent.putExtra("EXTRA_INCOMING_CALL_ID", call.getCallId());
        ...
        getApplicationContext().startActivity(intent);
    }
});

// {YourCallActivity}.java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ...
    String callId = getIntent().getStringExtra("EXTRA_INCOMING_CALL_ID");
    DirectCall call = SendBirdCall.getCall(callId);
    ...
    SendBirdVideoView localVideoView = findViewById(R.id.video_view_fullscreen);
    call.setLocalVideoView(localVideoView);
    ...
}
```

```xml
// {activity_your_call}.xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    ...
    <com.sendbird.calls.SendBirdVideoView
        android:id="@+id/video_view_fullscreen"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    ...
</RelativeLayout>
```

## Reference

- [SendBird Calls SDK for Android](https://github.com/sendbird/sendbird-calls-android)

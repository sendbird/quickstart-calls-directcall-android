# Sendbird Calls for Android Quickstart

![Platform](https://img.shields.io/badge/platform-ANDROID-orange.svg)
![Languages](https://img.shields.io/badge/language-JAVA-orange.svg)

[![Download:
Google Play](https://lh3.googleusercontent.com/cjsqrWQKJQp9RFO7-hJ9AfpKzbUb_Y84vXfjlP0iRHBvladwAfXih984olktDhPnFqyZ0nu9A5jvFwOEQPXzv7hr3ce3QVsLN8kQ2Ao=s0)](https://play.google.com/store/apps/details?id=com.sendbird.calls.quickstart)

## Introduction

Sendbird Calls SDK for Android is used to initialize, configure, and build voice and video calling functionality into your Android client app. In this repository, you will find the steps you need to take before implementing the Calls SDK into a project, and a sample app which contains the code for implementing voice and video call.

### More about Sendbird Calls for Android

Find out more about Sendbird Calls for Android on [Calls for Android doc](https://docs.sendbird.com/android/calls_quick_start). If you need any help in resolving any issues or have questions, visit [our community](https://community.sendbird.com).

<br />

## Before getting started

This section shows you the prerequisites you need for testing Sendbird Calls for Android sample app.

### Requirements

The minimum requirements for Calls SDK for Android sample are:

- Android 4.1 (API level 16) or higher
- Java 8 or higher
- Gradle 3.4.0 or higher
- Calls SDK for Android 1.0.3 or higher

For more details on **installing and configuring the Calls SDK for Android**, refer to [Calls for Android doc](https://docs.sendbird.com/android/calls_quick_start#3_install_and_configure_the_calls_sdk).

<br />

## Getting started

If you would like to try the sample app specifically fit to your usage, you can do so by following the steps below.

### Create a Sendbird application

 1. Login or Sign-up for an account on [Sendbird Dashboard](https://dashboard.sendbird.com).
 2. Create or select an application on the dashboard.
 3. Note your Sendbird application ID for future reference.
 4. [Contact sales](https://get.sendbird.com/talk-to-sales.html) to get the **Calls** menu enabled on your dashboard. A **self-serve** will be available soon to help you purchase call credits automatically from your dashboard.  

### Create test users

 1. On the Sendbird dashboard, navigate to the **Users** menu.
 2. Create at least two new users: one as a `caller`, and the other as a `callee`.
 3. Note the `user_id` of each user for future reference.

### Specify the Application ID

To run the sample Android app on the Sendbird application specified earlier, your Sendbird application ID must be specified. On the sample client app’s source code, replace `SAMPLE_APP_ID` with `APP_ID` which you can find on your Sendbird application information. 

```java
public class BaseApplication extends Application {
    ...
    
    private static final String APP_ID = "SAMPLE_APP_ID";
    ...
    
}
```

### Build and run the sample app

1. Build and run the sample app on your Android device.
2. Install the application onto at least two separate devices for each test user you created earlier.
3. If there are no two devices available, you can use an emulator to run the application instead.

For more detail on how to build and run an Android application, refer to [Android Documentation](https://developer.android.com/studio/run).

<br />

## Making your first call

### How to make a call

1. Log in to the sample app on the primary device with the user ID set as the `caller`.
2. Log in to the sample app on the secondary device using the ID of the user set as the `callee`. Alternatively, you can also use the Calls widget found on the Calls dashboard to log in as the `callee`.
3. On the primary device, specify the user ID of the `callee` and initiate a call.
4. If all steps are followed correctly, an incoming call notification will appear on the device of the `callee`.
5. Reverse the roles. Initiate a call from the other device.
6. If the two testing devices are near each other, use headphones to make a call to prevent audio feedback.

<br />

## Advanced

### Create a local video view before accepting an incoming call

You can create how the current user’s local video view will show on the screen before accepting an incoming call. Follow the steps below to customize the current user’s local video view:

1. Start your call activity with an incoming call ID in the `onRinging()` method within the class where you implement code for receiving a call. 
2. Get the `DirectCall` object with the incoming call ID within the `onCreate()` method of the call activity class.
3. Get the `SendBirdVideoView` object from the `xml` file of your call activity to add a local video view.
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

<br />

### Sound Effects

You can use different sound effects to enhance the user experience for events that take place while using Sendbird Calls.

To add sound effects, use the `SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType soundType, int resId)` method for the following events: dialing, ringing, reconnecting, and reconnected. Remember to set sound effects before the mentioned events occur. To remove sound effects, use the `SendBirdCall.Options.removeDirectCallSound(SendBirdCall.SoundType soundType)` method.

```java
// Play on a caller’s side when making a call.
SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType.DIALING, R.raw.dialing);
// Play on a callee’s side when receiving a call.
SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType.RINGING, R.raw.ringing);
// Play when a connection is lost, but the SDK immediately attempts to reconnect.
SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType.RECONNECTING, R.raw.reconnecting);
// Play when the connection is re-established.
SendBirdCall.Options.addDirectCallSound(SendBirdCall.SoundType.RECONNECTED, R.raw.reconnected);
```

<br />

## Reference

For further detail on Sendbird Calls for Android, refer to [Sendbird Calls SDK for Android README](https://github.com/sendbird/sendbird-calls-android/blob/master/README.md).

# SendBird Calls QuickStart Guide for Android

## Introduction

The `SendBirdCalls` framework enables realtime VoIP communication between app users.  The repository contains a sample application intended to demonstrate a simple implementation of this framework. This document details how to get up and running using this sample application.

## Prerequisites

- Android 4.1 (API level 16) or later

## Creating a SendBird application

 1. Login or Sign-up for an account at [dashboard](https://dashboard.sendbird.com).
 2. Create or select an application on the SendBird Dashboard.
 3. Note the `Application ID` for future reference.
 4. Contact sales to enable Calls in your application.

## Creating test users

 1. In the SendBird dashboard, navigate to the `Users` menu.
 2. Create at least two new users, one that will be the `caller`, and one that will be the `callee`.
 3. Note the `User ID` of each user for future reference.

## Specifying the App ID

To connect this sample Android application to the SendBird application specified in the previous step, the **App ID** of the SendBird application must be specified inside the sample Android application’s source code.

Replace `YOUR_APP_ID` with the `App ID` of the SendBird application specified in the previous step.

```java
public class BaseApplication extends Application {
    ...
    private static final String APP_ID = "YOUR_APP_ID";
    ...
}
```

## Making calls

 1. Log in to the primary device’s sample application with the ID of the user designated as the `caller`.
 2. Log in to the secondary device’s sample application with ID of the user designated as the `callee`.  Alternatively, use the Calls widget found on the Calls dashboard to login as the `callee`.
 3. On the primary device, specify the user ID of the `callee` and initiate a call.
 4. If all steps have been followed correctly, an incoming call notification will appear on the `callee` user’s device.
 5. Reverse roles, and initiate a call from the other device.
 6. If the `caller` and `callee` devices are near each other, use headphones to prevent audio feedback.
 7. The SendBird Calls Android Sample has been successfully implemented.

## Reference

- [SendBird Calls SDK for Android](https://github.com/sendbird/sendbird-calls-android).

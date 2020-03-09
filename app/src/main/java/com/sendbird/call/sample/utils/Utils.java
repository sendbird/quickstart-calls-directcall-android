package com.sendbird.call.sample.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.Locale;

public class Utils {

    public static String getTimeString(long periodMs) {
        final String result;
        int totalSec = (int)(periodMs / 1000);
        int hour = 0, min, sec;

        if (totalSec >= 3600) {
            hour = totalSec / 3600;
            totalSec = totalSec % 3600;
        }

        min = totalSec / 60;
        sec = totalSec % 60;

        if (hour > 0) {
            result = String.format(Locale.getDefault(), "%d:%02d:%02d", hour, min, sec);
        } else if (min > 0) {
            result = String.format(Locale.getDefault(), "%d:%02d", min, sec);
        } else {
            result = String.format(Locale.getDefault(), "0:%02d", sec);
        }
        return result;
    }

    public static void displayRoundImageFromUrl(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .asBitmap()
                .apply(new RequestOptions().centerCrop().dontAnimate())
                .load(url)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}

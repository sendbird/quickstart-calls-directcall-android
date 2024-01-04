package com.sendbird.calls.quickstart.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class ViewPagerWithSwipeOption extends ViewPager {

    private boolean mIsSwipeEnabled = true;

    public ViewPagerWithSwipeOption(@NonNull Context context) {
        super(context);
    }

    public ViewPagerWithSwipeOption(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsSwipeEnabled) {
            return super.onInterceptTouchEvent(ev);
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIsSwipeEnabled) {
            return super.onTouchEvent(ev);
        }
        return false;
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, false);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item, false);
    }
}

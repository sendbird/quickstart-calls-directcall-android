package com.sendbird.calls.quickstart.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.quickstart.R;
import com.sendbird.calls.quickstart.utils.ToastUtils;
import com.sendbird.calls.quickstart.utils.UserInfoUtils;

public class MainActivity extends AppCompatActivity {

    public static final boolean sHistoryFeature = false;

    private static final String TAG = "MainActivity";
    private static final int REQUEST_OVERLAY_PERMISSION = 1;

    private Context mContext;
    private LinearLayout mLinearLayoutToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mLinearLayoutToolbar = findViewById(R.id.linear_layout_toolbar);

        initViews();
        checkOverlayPermission();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        UserInfoUtils.setProfileImage(mContext, SendBirdCall.getCurrentUser(), findViewById(R.id.image_view_profile));
        UserInfoUtils.setUserId(SendBirdCall.getCurrentUser(), findViewById(R.id.text_view_user_id));

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);

        PagerAdapter pagerAdapter = new DialFragmentPagerAdapter(mContext, getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        int tabIndex = 0;
        tabLayout.getTabAt(tabIndex).setIcon(R.drawable.ic_call_filled).setText(null);
        if (sHistoryFeature) {
            tabIndex++;
            tabLayout.getTabAt(tabIndex).setIcon(R.drawable.ic_layout_default).setText(null);
        }
        tabIndex++;
        tabLayout.getTabAt(tabIndex).setIcon(R.drawable.ic_settings).setText(null);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(pagerAdapter.getPageTitle(0));
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                toolbar.setTitle(pagerAdapter.getPageTitle(position));

                if (sHistoryFeature) {
                    switch (position) {
                        case 0:
                            mLinearLayoutToolbar.setVisibility(View.VISIBLE);
                            tabLayout.getTabAt(0).setIcon(R.drawable.ic_call_filled);
                            tabLayout.getTabAt(1).setIcon(R.drawable.ic_layout_default);
                            tabLayout.getTabAt(2).setIcon(R.drawable.ic_settings);
                            break;
                        case 1:
                            mLinearLayoutToolbar.setVisibility(View.GONE);
                            tabLayout.getTabAt(0).setIcon(R.drawable.ic_call);
                            tabLayout.getTabAt(1).setIcon(R.drawable.icon_call_history);
                            tabLayout.getTabAt(2).setIcon(R.drawable.ic_settings);
                            break;
                        case 2:
                            mLinearLayoutToolbar.setVisibility(View.GONE);
                            tabLayout.getTabAt(0).setIcon(R.drawable.ic_call);
                            tabLayout.getTabAt(1).setIcon(R.drawable.ic_layout_default);
                            tabLayout.getTabAt(2).setIcon(R.drawable.icon_settings_filled);
                            break;
                    }
                } else {
                    switch (position) {
                        case 0:
                            mLinearLayoutToolbar.setVisibility(View.VISIBLE);
                            tabLayout.getTabAt(0).setIcon(R.drawable.ic_call_filled);
                            tabLayout.getTabAt(1).setIcon(R.drawable.ic_settings);
                            break;
                        case 1:
                            mLinearLayoutToolbar.setVisibility(View.GONE);
                            tabLayout.getTabAt(0).setIcon(R.drawable.ic_call);
                            tabLayout.getTabAt(1).setIcon(R.drawable.icon_settings_filled);
                            break;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Settings.canDrawOverlays(mContext)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + mContext.getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION); // To start CallActivity on background when ringing.
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mContext != null && !Settings.canDrawOverlays(mContext)) {
                Log.d(TAG, "Overlay permission denied.");
                ToastUtils.showToast(mContext, "Overlay permission denied.");
                finish();
            }
        }
    }
}
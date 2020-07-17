package com.sendbird.calls.quickstart.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sendbird.calls.DirectCallLog;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.quickstart.BaseApplication;
import com.sendbird.calls.quickstart.R;
import com.sendbird.calls.quickstart.utils.BroadcastUtils;
import com.sendbird.calls.quickstart.utils.ToastUtils;
import com.sendbird.calls.quickstart.utils.UserInfoUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String[] MANDATORY_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,   // for VoiceCall and VideoCall
            Manifest.permission.CAMERA          // for VideoCall
    };
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private Context mContext;

    private LinearLayout mLinearLayoutToolbar;
    private MainPagerAdapter mMainPagerAdapter;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mLinearLayoutToolbar = findViewById(R.id.linear_layout_toolbar);

        initViews();
        registerReceiver();
        checkPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        UserInfoUtils.setProfileImage(mContext, SendBirdCall.getCurrentUser(), findViewById(R.id.image_view_profile));
        UserInfoUtils.setNickname(mContext, SendBirdCall.getCurrentUser(), findViewById(R.id.text_view_nickname));
        UserInfoUtils.setUserId(mContext, SendBirdCall.getCurrentUser(), findViewById(R.id.text_view_user_id));

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);

        mMainPagerAdapter = new MainPagerAdapter(mContext, getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(mMainPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        int tabIndex = 0;
        tabLayout.getTabAt(tabIndex).setIcon(R.drawable.ic_call_filled).setText(null);
        tabIndex++;
        tabLayout.getTabAt(tabIndex).setIcon(R.drawable.ic_layout_default).setText(null);
        tabIndex++;
        tabLayout.getTabAt(tabIndex).setIcon(R.drawable.ic_settings).setText(null);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mMainPagerAdapter.getPageTitle(0));
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                toolbar.setTitle(mMainPagerAdapter.getPageTitle(position));

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
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void registerReceiver() {
        Log.i(BaseApplication.TAG, "[MainActivity] registerReceiver()");

        if (mReceiver != null) {
            return;
        }

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(BaseApplication.TAG, "[MainActivity] onReceive()");

                DirectCallLog callLog = (DirectCallLog)intent.getSerializableExtra(BroadcastUtils.INTENT_EXTRA_CALL_LOG);
                if (callLog != null) {
                    HistoryFragment historyFragment = (HistoryFragment) mMainPagerAdapter.getItem(1);
                    historyFragment.addLatestCallLog(callLog);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastUtils.INTENT_ACTION_ADD_CALL_LOG);
        registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        Log.i(BaseApplication.TAG, "[MainActivity] unregisterReceiver()");

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
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
                ToastUtils.showToast(mContext, "Permission denied.");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            boolean allowed = true;

            for (int result : grantResults) {
                allowed = allowed && (result == PackageManager.PERMISSION_GRANTED);
            }

            if (!allowed) {
                ToastUtils.showToast(mContext, "Permission denied.");
            }
        }
    }
}
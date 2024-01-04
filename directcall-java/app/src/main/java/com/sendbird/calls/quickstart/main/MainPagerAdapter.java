package com.sendbird.calls.quickstart.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sendbird.calls.quickstart.R;

import java.util.ArrayList;
import java.util.List;

class MainPagerAdapter extends FragmentPagerAdapter {

    private final List<FragmentInfo> mFragmentList = new ArrayList<>();

    private static class FragmentInfo {
        final String mTitle;
        final Fragment mFragment;

        FragmentInfo(String title, Fragment fragment) {
            mTitle = title;
            mFragment = fragment;
        }
    }

    MainPagerAdapter(@NonNull Context context, @NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);

        mFragmentList.add(new FragmentInfo("", new DialFragment()));
        mFragmentList.add(new FragmentInfo(context.getString(R.string.calls_history), new HistoryFragment()));
        mFragmentList.add(new FragmentInfo(context.getString(R.string.calls_settings), new SettingsFragment()));
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position).mFragment;
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentList.get(position).mTitle;
    }
}

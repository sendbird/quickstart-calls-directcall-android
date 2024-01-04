package com.sendbird.calls.quickstart.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.sendbird.calls.quickstart.R

internal class MainPagerAdapter(context: Context, fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {
    private class FragmentInfo(val mTitle: String, val mFragment: Fragment)

    private val mFragmentList: MutableList<FragmentInfo> = ArrayList<FragmentInfo>().apply {
        this.add(FragmentInfo("", DialFragment()))
        this.add(FragmentInfo(context.getString(R.string.calls_history), HistoryFragment()))
        this.add(FragmentInfo(context.getString(R.string.calls_settings), SettingsFragment()))
    }

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position].mFragment
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mFragmentList[position].mTitle
    }
}

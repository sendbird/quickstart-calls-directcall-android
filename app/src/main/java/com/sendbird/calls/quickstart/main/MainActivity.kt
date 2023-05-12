package com.sendbird.calls.quickstart.main

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.sendbird.calls.DirectCallLog
import com.sendbird.calls.SendBirdCall.currentUser
import com.sendbird.calls.quickstart.R
import com.sendbird.calls.quickstart.TAG
import com.sendbird.calls.quickstart.databinding.ActivityMainBinding
import com.sendbird.calls.quickstart.utils.*

private const val REQUEST_PERMISSIONS_REQUEST_CODE = 1
private val MANDATORY_PERMISSIONS = arrayOf(
    Manifest.permission.RECORD_AUDIO,  // for VoiceCall and VideoCall
    Manifest.permission.CAMERA,  // for VideoCall
    Manifest.permission.BLUETOOTH // for VoiceCall and VideoCall
)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mMainPagerAdapter: MainPagerAdapter? = null
    private var mReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUI()
        registerReceiver()
        checkPermissions()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(TAG, "[MainActivity] onNewIntent()")
        setUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
    }

    private fun setUI() {
        setSupportActionBar(binding.toolbar)
        binding.linearLayoutToolbar.visibility = View.VISIBLE
        setProfileImage(currentUser, binding.imageViewProfile)
        setNickname(currentUser, binding.textViewNickname)
        setUserId(currentUser, binding.textViewUserId)
        mMainPagerAdapter = MainPagerAdapter(this, supportFragmentManager, binding.tabLayout.tabCount)
        binding.viewPager.adapter = mMainPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_call_filled)?.text = null
        binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_layout_default)?.text = null
        binding.tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_settings)?.text = null
        supportActionBar?.title = mMainPagerAdapter?.getPageTitle(binding.viewPager.currentItem)
        binding.viewPager.addOnPageChangeListener(object : OnPageChangeListener {

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                binding.toolbar.title = mMainPagerAdapter?.getPageTitle(position)
                when (position) {
                    0 -> {
                        binding.linearLayoutToolbar.visibility = View.VISIBLE
                        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_call_filled)
                        binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_layout_default)
                        binding.tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_settings)
                    }
                    1 -> {
                        binding.linearLayoutToolbar.visibility = View.GONE
                        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_call)
                        binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.icon_call_history)
                        binding.tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_settings)
                    }
                    2 -> {
                        binding.linearLayoutToolbar.visibility = View.GONE
                        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_call)
                        binding.tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_layout_default)
                        binding.tabLayout.getTabAt(2)?.setIcon(R.drawable.icon_settings_filled)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun registerReceiver() {
        Log.i(TAG, "[MainActivity] registerReceiver()")
        if (mReceiver != null) return
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i(TAG, "[MainActivity] onReceive()")
                val callLog = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(INTENT_ACTION_ADD_CALL_LOG, DirectCallLog::class.java)
                } else {
                    intent.getSerializableExtra(INTENT_ACTION_ADD_CALL_LOG) as DirectCallLog
                }
                if (callLog != null) {
                    val historyFragment = mMainPagerAdapter?.getItem(1) as HistoryFragment
                    historyFragment.addLatestCallLog(callLog)
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(INTENT_ACTION_ADD_CALL_LOG)
        registerReceiver(mReceiver, intentFilter)
    }

    private fun unregisterReceiver() {
        Log.i(TAG, "[MainActivity] unregisterReceiver()")
        mReceiver ?: return
        unregisterReceiver(mReceiver)
        mReceiver = null
    }

    private fun checkPermissions() {
        val permissions = ArrayList(listOf(*MANDATORY_PERMISSIONS))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val deniedPermissions = ArrayList<String>()
        for (permission in permissions) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission)
            }
        }
        if (deniedPermissions.size > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(deniedPermissions.toTypedArray(), REQUEST_PERMISSIONS_REQUEST_CODE)
            } else {
                showToast("Permission denied.")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            var allowed = true
            for (result in grantResults) {
                allowed = allowed && result == PackageManager.PERMISSION_GRANTED
            }
            if (!allowed) {
                showToast("Permission denied.")
            }
        }
    }
}
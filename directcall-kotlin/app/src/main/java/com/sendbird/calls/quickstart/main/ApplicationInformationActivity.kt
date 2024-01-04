package com.sendbird.calls.quickstart.main

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.calls.quickstart.R
import com.sendbird.calls.quickstart.databinding.ActivityApplicationInformationBinding
import com.sendbird.calls.quickstart.utils.getAppId

class ApplicationInformationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationInformationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.title = getString(R.string.calls_application_information)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.icon_arrow_left)
        }
        binding.textViewApplicationId.text = getAppId()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
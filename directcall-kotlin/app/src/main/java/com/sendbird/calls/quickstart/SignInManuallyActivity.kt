package com.sendbird.calls.quickstart

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.sendbird.calls.quickstart.databinding.ActivitySignInManuallyBinding
import com.sendbird.calls.quickstart.utils.*

class SignInManuallyActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInManuallyBinding
    private lateinit var mInputMethodManager: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInManuallyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mInputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        initViews()
    }

    private fun initViews() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.title = getString(R.string.calls_sign_in_manually_title)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.icon_close)
        }
        val savedAppId = getAppId()
        if (!savedAppId.isNullOrEmpty() && savedAppId != "YOUR_APPLICATION_ID") {
            binding.textInputEditTextAppId.setText(savedAppId)
        }
        val savedUserId = getUserId()
        if (!savedAppId.isNullOrEmpty()) {
            binding.textInputEditTextUserId.setText(savedUserId)
        }
        val savedAccessToken = getAccessToken()
        if (!savedAppId.isNullOrEmpty()) {
            binding.textInputEditTextAccessToken.setText(savedAccessToken)
        }
        checkSignInStatus()
        binding.textInputEditTextAppId.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.textInputEditTextAppId.clearFocus()
                mInputMethodManager.hideSoftInputFromWindow(binding.textInputEditTextAppId.windowToken,0)
                return@setOnEditorActionListener true
            }
            false
        }
        binding.textInputEditTextAppId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable) {
                checkSignInStatus()
            }
        })
        binding.textInputEditTextUserId.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.textInputEditTextUserId.clearFocus()
                mInputMethodManager.hideSoftInputFromWindow(binding.textInputEditTextUserId.windowToken,0)
                return@setOnEditorActionListener true
            }
            false
        }
        binding.textInputEditTextUserId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable) {
                checkSignInStatus()
            }
        })
        binding.textInputEditTextAccessToken.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.textInputEditTextAccessToken.clearFocus()
                mInputMethodManager.hideSoftInputFromWindow(binding.textInputEditTextAccessToken.windowToken,0)
                return@setOnEditorActionListener true
            }
            false
        }
        binding.relativeLayoutSignIn.setOnClickListener {
            val appId = binding.textInputEditTextAppId.text.toString()
            val userId = binding.textInputEditTextUserId.text.toString()
            val accessToken = binding.textInputEditTextAccessToken.text.toString()
            if (appId.isNotEmpty() && userId.isNotEmpty() && (application as BaseApplication).initSendBirdCall(appId)) {
                AuthenticationUtils.authenticate(this, userId, accessToken) { isSuccess ->
                    if (isSuccess) {
                        setResult(RESULT_OK, null)
                        startMainActivityAndFinish()
                    } else {
                        binding.textInputLayoutAppId.isEnabled = true
                        binding.textInputLayoutUserId.isEnabled = true
                        binding.textInputLayoutAccessToken.isEnabled = true
                        binding.relativeLayoutSignIn.isEnabled = true
                    }
                }
                binding.textInputLayoutAppId.isEnabled = false
                binding.textInputLayoutUserId.isEnabled = false
                binding.textInputLayoutAccessToken.isEnabled = false
                binding.relativeLayoutSignIn.isEnabled = false
            }
        }
    }

    private fun checkSignInStatus() {
        val appId = binding.textInputEditTextAppId.text.toString()
        val userId = binding.textInputEditTextUserId.text.toString()
        binding.relativeLayoutSignIn.isEnabled = appId.isNotEmpty() && userId.isNotEmpty()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
package com.sendbird.calls.quickstart

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.calls.SendBirdCall.getSdkVersion
import com.sendbird.calls.quickstart.databinding.ActivityAuthenticateBinding
import com.sendbird.calls.quickstart.utils.QRCodeUtils
import com.sendbird.calls.quickstart.utils.START_SIGN_IN_MANUALLY_ACTIVITY_REQUEST_CODE
import com.sendbird.calls.quickstart.utils.startMainActivityAndFinish
import com.sendbird.calls.quickstart.utils.startSignInManuallyActivityForResult

class AuthenticateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        //+ [QRCode]
        binding.relativeLayoutSignInWithQrcode.setOnClickListener {
            binding.relativeLayoutSignInWithQrcode.isEnabled = false
            binding.relativeLayoutSignInManually.isEnabled = false
            QRCodeUtils.scanQRCode(this@AuthenticateActivity)
        }
        //- [QRCode]
        binding.relativeLayoutSignInManually.setOnClickListener {
            startSignInManuallyActivityForResult()
        }
        binding.textViewQuickstartVersion.text = getString(R.string.calls_quickstart_version, VERSION)
        binding.textViewSdkVersion.text = getString(R.string.calls_sdk_version, getSdkVersion())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == START_SIGN_IN_MANUALLY_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                finish()
            }
            return
        }

        //+ [QRCode]
        QRCodeUtils.onActivityResult(
            this@AuthenticateActivity,
            requestCode,
            resultCode,
            data,
            object : QRCodeUtils.CompletionHandler {
                override fun onCompletion(isSuccess: Boolean) {
                    if (isSuccess) {
                        startMainActivityAndFinish()
                    } else {
                        binding.relativeLayoutSignInWithQrcode.isEnabled = true
                        binding.relativeLayoutSignInManually.isEnabled = true
                    }
                }

            }
        )
        //- [QRCode]
    }
}
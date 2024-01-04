package com.sendbird.calls.quickstart.main

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.sendbird.calls.quickstart.BaseFragment
import com.sendbird.calls.quickstart.call.CallService
import com.sendbird.calls.quickstart.databinding.FragmentDialBinding
import com.sendbird.calls.quickstart.utils.getCalleeId
import com.sendbird.calls.quickstart.utils.setCalleeId

class DialFragment : BaseFragment<FragmentDialBinding>(FragmentDialBinding::inflate) {
    private lateinit var mInputMethodManager: InputMethodManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mInputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.imageViewVideoCall.isEnabled = false
        binding.imageViewVoiceCall.isEnabled = false
        val savedCalleeId = getCalleeId()
        if (!savedCalleeId.isNullOrEmpty()) {
            binding.textInputEditTextUserId.setText(savedCalleeId)
            binding.textInputEditTextUserId.setSelection(savedCalleeId.length)
            binding.imageViewVideoCall.isEnabled = true
            binding.imageViewVoiceCall.isEnabled = true
        }
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
                binding.imageViewVideoCall.isEnabled = editable.isNotEmpty()
                binding.imageViewVoiceCall.isEnabled = editable.isNotEmpty()
            }
        })
        binding.imageViewVideoCall.setOnClickListener {
            val calleeId = if (!binding.textInputEditTextUserId.text.isNullOrEmpty()) binding.textInputEditTextUserId.text.toString() else ""
            if (calleeId.isNotEmpty()) {
                CallService.dial(requireContext(), calleeId, true)
                setCalleeId(calleeId)
            }
        }
        binding.imageViewVoiceCall.setOnClickListener {
            val calleeId = if (!binding.textInputEditTextUserId.text.isNullOrEmpty()) binding.textInputEditTextUserId.text.toString() else ""
            if (calleeId.isNotEmpty()) {
                CallService.dial(requireContext(), calleeId, false)
                setCalleeId(calleeId)
            }
        }
    }
}
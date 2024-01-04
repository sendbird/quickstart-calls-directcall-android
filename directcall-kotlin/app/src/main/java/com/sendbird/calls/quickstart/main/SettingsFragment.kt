package com.sendbird.calls.quickstart.main

import android.os.Bundle
import android.view.View
import com.sendbird.calls.SendBirdCall.currentUser
import com.sendbird.calls.quickstart.BaseFragment
import com.sendbird.calls.quickstart.databinding.FragmentSettingsBinding
import com.sendbird.calls.quickstart.utils.AuthenticationUtils.deauthenticate
import com.sendbird.calls.quickstart.utils.setNickname
import com.sendbird.calls.quickstart.utils.setProfileImage
import com.sendbird.calls.quickstart.utils.startApplicationInformationActivity
import com.sendbird.calls.quickstart.utils.startAuthenticateActivityAndFinish

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = currentUser
        if (currentUser != null) {
            setProfileImage(currentUser, binding.imageViewProfile)
            setNickname(currentUser, binding.textViewNickname)
            binding.textViewUserId.text = currentUser.userId
        }
        binding.linearLayoutApplicationInformation.setOnClickListener {
            startApplicationInformationActivity()
        }
        binding.linearLayoutSignOut.setOnClickListener {
            deauthenticate(requireContext()) {
                startAuthenticateActivityAndFinish()
            }
        }
    }
}
package com.sendbird.calls.quickstart.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sendbird.calls.DirectCallLog
import com.sendbird.calls.DirectCallUser
import com.sendbird.calls.DirectCallUserRole
import com.sendbird.calls.quickstart.R
import com.sendbird.calls.quickstart.call.CallService
import com.sendbird.calls.quickstart.databinding.FragmentHistoryRecyclerViewItemBinding
import com.sendbird.calls.quickstart.main.HistoryRecyclerViewAdapter.HistoryViewHolder
import com.sendbird.calls.quickstart.utils.toDateString
import com.sendbird.calls.quickstart.utils.toTimeStringForHistory
import com.sendbird.calls.quickstart.utils.displayCircularImageFromUrl
import com.sendbird.calls.quickstart.utils.getEndResultString
import com.sendbird.calls.quickstart.utils.setNickname
import com.sendbird.calls.quickstart.utils.setUserId

internal class HistoryRecyclerViewAdapter : RecyclerView.Adapter<HistoryViewHolder>() {
    private val mDirectCallLogs: MutableList<DirectCallLog> = ArrayList()

    fun setCallLogs(callLogs: List<DirectCallLog>?) {
        mDirectCallLogs.clear()
        callLogs ?: return
        mDirectCallLogs.addAll(callLogs)
    }

    fun addCallLogs(callLogs: List<DirectCallLog>?) {
        callLogs ?: return
        mDirectCallLogs.addAll(callLogs)
    }

    fun addLatestCallLog(callLog: DirectCallLog) {
        mDirectCallLogs.add(0, callLog)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = FragmentHistoryRecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val callLog = mDirectCallLogs[position]
        holder.bind(callLog)
    }

    override fun getItemCount(): Int {
        return mDirectCallLogs.size
    }

    internal class HistoryViewHolder(private val binding: FragmentHistoryRecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(callLog: DirectCallLog) {
            val myRole = callLog.myRole
            val user: DirectCallUser?
            val context = binding.root.context
            if (myRole == DirectCallUserRole.CALLER) {
                user = callLog.callee
                binding.imageViewIncomingOrOutgoing.setBackgroundResource(if (callLog.isVideoCall) R.drawable.icon_call_video_outgoing_filled else R.drawable.icon_call_voice_outgoing_filled)
            } else {
                user = callLog.caller
                binding.imageViewIncomingOrOutgoing.setBackgroundResource(if (callLog.isVideoCall) R.drawable.icon_call_video_incoming_filled else R.drawable.icon_call_voice_incoming_filled)
            }
            if (user != null) {
                context.displayCircularImageFromUrl(user.profileUrl, binding.imageViewProfile)
            }
            context.setNickname(user, binding.textViewNickname)
            context.setUserId(user, binding.textViewUserId)
            val endResult = context.getEndResultString(callLog.endResult)
            val endResultAndDuration = endResult + context.getString(R.string.calls_and_character) + callLog.duration.toTimeStringForHistory()
            binding.textViewEndResultAndDuration.text = endResultAndDuration
            binding.textViewStartAt.text = callLog.startedAt.toDateString()
            binding.imageViewVideoCall.setOnClickListener {
                val userId = user?.userId ?: return@setOnClickListener
                CallService.dial(context, userId, true)
            }
            binding.imageViewVoiceCall.setOnClickListener {
                val userId = user?.userId ?: return@setOnClickListener
                CallService.dial(context, userId,false)
            }
        }
    }
}
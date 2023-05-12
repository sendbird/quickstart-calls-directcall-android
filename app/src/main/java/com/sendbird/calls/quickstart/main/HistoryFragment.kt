package com.sendbird.calls.quickstart.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.sendbird.calls.DirectCallLog
import com.sendbird.calls.DirectCallLogListQuery
import com.sendbird.calls.SendBirdCall.createDirectCallLogListQuery
import com.sendbird.calls.quickstart.BaseFragment
import com.sendbird.calls.quickstart.databinding.FragmentHistoryBinding
import com.sendbird.calls.quickstart.utils.showToast

class HistoryFragment : BaseFragment<FragmentHistoryBinding>(FragmentHistoryBinding::inflate) {
    private lateinit var mRecyclerViewLinearLayoutManager: LinearLayoutManager
    private lateinit var mRecyclerViewHistoryAdapter: HistoryRecyclerViewAdapter
    private var mDirectCallLogListQuery: DirectCallLogListQuery? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerViewHistoryAdapter = HistoryRecyclerViewAdapter()
        binding.recyclerViewHistory.adapter = mRecyclerViewHistoryAdapter
        mRecyclerViewLinearLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistory.layoutManager = mRecyclerViewLinearLayoutManager
        binding.recyclerViewHistory.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastItemPosition = mRecyclerViewLinearLayoutManager.findLastVisibleItemPosition()
                if (lastItemPosition >= 0 && lastItemPosition == mRecyclerViewHistoryAdapter.itemCount - 1) {
                    val mDirectCallLogListQuery = mDirectCallLogListQuery ?: return
                    if (mDirectCallLogListQuery.hasNext() && !mDirectCallLogListQuery.isLoading) {
                        binding.progressBar.visibility = View.VISIBLE
                        mDirectCallLogListQuery.next { callLogs, e ->
                            binding.progressBar.visibility = View.GONE
                            if (e != null) {
                                showToast(e.message ?: "")
                                return@next
                            }
                            if (!callLogs.isNullOrEmpty()) {
                                val positionStart = mRecyclerViewHistoryAdapter.itemCount
                                mRecyclerViewHistoryAdapter.addCallLogs(callLogs)
                                mRecyclerViewHistoryAdapter.notifyItemRangeChanged(positionStart, callLogs.size)
                            }
                        }
                    }
                }
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.recyclerViewHistory.visibility = View.GONE
        binding.linearLayoutEmpty.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        mDirectCallLogListQuery = createDirectCallLogListQuery(DirectCallLogListQuery.Params().setLimit(20))
        mDirectCallLogListQuery?.next { callLogs, e ->
            binding.progressBar.visibility = View.GONE
            if (e != null) {
                showToast(e.message ?: "")
                return@next
            }
            if (!callLogs.isNullOrEmpty()) {
                binding.recyclerViewHistory.visibility = View.VISIBLE
                binding.linearLayoutEmpty.visibility = View.GONE
                mRecyclerViewHistoryAdapter.setCallLogs(callLogs)
                mRecyclerViewHistoryAdapter.notifyDataSetChanged()
            } else {
                binding.recyclerViewHistory.visibility = View.GONE
                binding.linearLayoutEmpty.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addLatestCallLog(callLog: DirectCallLog?) {
        callLog ?: return
        mRecyclerViewHistoryAdapter.addLatestCallLog(callLog)
        mRecyclerViewHistoryAdapter.notifyDataSetChanged()
    }
}
package com.sendbird.calls.quickstart.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sendbird.calls.DirectCallLog;
import com.sendbird.calls.DirectCallLogListQuery;
import com.sendbird.calls.SendBirdCall;
import com.sendbird.calls.quickstart.R;
import com.sendbird.calls.quickstart.utils.ToastUtils;

public class HistoryFragment extends Fragment {

    private ProgressBar mProgressBar;
    private LinearLayout mLinearLayoutEmpty;

    private RecyclerView mRecyclerViewHistory;
    private HistoryRecyclerViewAdapter mRecyclerViewHistoryAdapter;
    private LinearLayoutManager mRecyclerViewLinearLayoutManager;

    private DirectCallLogListQuery mDirectCallLogListQuery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mProgressBar = view.findViewById(R.id.progress_bar);
        mLinearLayoutEmpty = view.findViewById(R.id.linear_layout_empty);

        mRecyclerViewHistory = view.findViewById(R.id.recycler_view_history);
        mRecyclerViewHistoryAdapter = new HistoryRecyclerViewAdapter(getContext());
        mRecyclerViewHistory.setAdapter(mRecyclerViewHistoryAdapter);
        mRecyclerViewLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerViewHistory.setLayoutManager(mRecyclerViewLinearLayoutManager);

        mRecyclerViewHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int lastItemPosition = mRecyclerViewLinearLayoutManager.findLastVisibleItemPosition();
                if (lastItemPosition >= 0 && lastItemPosition == mRecyclerViewHistoryAdapter.getItemCount() - 1) {
                    if (mDirectCallLogListQuery != null && mDirectCallLogListQuery.hasNext() && !mDirectCallLogListQuery.isLoading()) {
                        mProgressBar.setVisibility(View.VISIBLE);

                        mDirectCallLogListQuery.next((list, e) -> {
                            mProgressBar.setVisibility(View.GONE);

                            if (e != null) {
                                ToastUtils.showToast(getContext(), e.getMessage());
                                return;
                            }

                            if (list.size() > 0) {
                                int positionStart = mRecyclerViewHistoryAdapter.getItemCount();
                                mRecyclerViewHistoryAdapter.addCallLogs(list);
                                mRecyclerViewHistoryAdapter.notifyItemRangeChanged(positionStart, list.size());
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecyclerViewHistory.setVisibility(View.GONE);
        mLinearLayoutEmpty.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        mDirectCallLogListQuery = SendBirdCall.createDirectCallLogListQuery(new DirectCallLogListQuery.Params().setLimit(20));
        mDirectCallLogListQuery.next((list, e) -> {
            mProgressBar.setVisibility(View.GONE);

            if (e != null) {
                ToastUtils.showToast(getContext(), e.getMessage());
                return;
            }

            if (list.size() > 0) {
                mRecyclerViewHistory.setVisibility(View.VISIBLE);
                mLinearLayoutEmpty.setVisibility(View.GONE);

                mRecyclerViewHistoryAdapter.setCallLogs(list);
                mRecyclerViewHistoryAdapter.notifyDataSetChanged();
            } else {
                mRecyclerViewHistory.setVisibility(View.GONE);
                mLinearLayoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    void addLatestCallLog(DirectCallLog callLog) {
        if (mRecyclerViewHistoryAdapter != null) {
            mRecyclerViewHistoryAdapter.addLatestCallLog(callLog);
            mRecyclerViewHistoryAdapter.notifyDataSetChanged();
        }
    }
}

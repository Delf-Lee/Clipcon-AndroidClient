package com.sprout.clipcon.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sprout.clipcon.R;
import com.sprout.clipcon.adapter.HistoryAdapter;
import com.sprout.clipcon.model.Contents;

import java.util.ArrayList;


/**
 * Created by Yongwon on 2017. 2. 8..
 */

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;

    private ArrayList<Contents> contentsArrayList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        setContentsCallback();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_history);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        historyAdapter = new HistoryAdapter(getContext(), contentsArrayList);
        recyclerView.setAdapter(historyAdapter);

        return view;
    }

    private void setContentsCallback() {
        // 임시주석
        /*Endpoint.ContentsCallback contentsResult = new Endpoint.ContentsCallback() {
            @Override
            public void onContentsUpdate(final Contents contents) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // update history ui
                        updateHistory(contents);
                    }
                });
            }
        };
        Endpoint.getInstance().setContentsCallback(contentsResult);*/
    }

    private void updateHistory(Contents contents) {
        contentsArrayList.add(contents);

        historyAdapter = new HistoryAdapter(getActivity(), contentsArrayList);
        recyclerView.setAdapter(historyAdapter);
    }
}

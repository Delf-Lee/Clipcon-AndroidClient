package com.sprout.clipcon.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sprout.clipcon.R;
import com.sprout.clipcon.model.Contents;

import java.util.ArrayList;

/**
 * Created by Yongwon on 2017. 4. 30..
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>{

    private Context context;
    private ArrayList<Contents> contentsList;

    public HistoryAdapter(Context context, ArrayList<Contents> contentsList) {
        this.context = context;
        this.contentsList = contentsList;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contents, null);
        HistoryViewHolder historyViewHolder = new HistoryViewHolder(view);

        return historyViewHolder;
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, final int position) {
        Contents contents = contentsList.get(position);
        holder.sender.setText(contents.getUploadUserName());
        holder.time.setText(contents.getUploadTime());

        switch(contents.getContentsType()) {
            case Contents.TYPE_IMAGE:
                holder.description.setText("image");
                holder.thumbnail.setImageResource(R.drawable.text_icon);
                break;
            case Contents.TYPE_FILE:
                holder.description.setText(contents.getContentsValue());
                holder.thumbnail.setImageResource(R.drawable.file_icon);
                break;
            case Contents.TYPE_STRING:
                holder.description.setText(contents.getContentsValue());
                holder.thumbnail.setImageResource(R.drawable.text_icon);
                //// TODO: 2017. 5. 16. should use glide
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, position+"번째가 클릭됐음", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return contentsList.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnail;
        TextView sender;
        TextView description;
        TextView time;

        public HistoryViewHolder(final View historyView) {
            super(historyView);
            thumbnail = (ImageView)historyView.findViewById(R.id.thumbnail);
            sender = (TextView)historyView.findViewById(R.id.contents_sender);
            description = (TextView)historyView.findViewById(R.id.contents_description);
            time = (TextView)historyView.findViewById(R.id.contents_time);
        }
    }
}

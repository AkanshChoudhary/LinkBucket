package com.example.linkbucket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.HistoryViewHolder> {
    List<HistoryItem> historyItems=new ArrayList<>();
    private OnHistoryClickedListener clickListener;

    public HistoryRecyclerAdapter(List<HistoryItem> historyItems) {
        this.historyItems = historyItems;
    }
    public void setOnHistoryClickedListener(OnHistoryClickedListener listener){
        clickListener=listener;
    }

    public interface OnHistoryClickedListener{
        void onFolderClicked(int position);
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View v = layoutInflater.inflate(R.layout.share_history_item, parent, false);
        return new HistoryRecyclerAdapter.HistoryViewHolder(v,clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem historyItem=historyItems.get(position);
        holder.bunch_name.setText(historyItem.getBunch_name());
        holder.info.setText(historyItem.getInfo());
        if(historyItem.getStatusCode()==0){holder.newTag.setVisibility(View.VISIBLE);}
        else if(historyItem.getStatusCode()==1){holder.newTag.setVisibility(View.INVISIBLE);}
        if(historyItem.isLink){
            holder.logo.setImageResource(R.drawable.ic_plane);
        }else{
            holder.logo.setImageResource(R.drawable.ic_folder_icon);
        }

    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder{
        TextView bunch_name,info;
        ImageView newTag;
        ImageView logo;
    public HistoryViewHolder(@NonNull View itemView,OnHistoryClickedListener listener) {
        super(itemView);
        bunch_name=itemView.findViewById(R.id.historyItemName);
        info=itemView.findViewById(R.id.historyItemInfo);
        newTag=itemView.findViewById(R.id.newTag);
        logo=itemView.findViewById(R.id.logo);
        itemView.setOnClickListener(v->listener.onFolderClicked(getAdapterPosition()));
    }
}
}

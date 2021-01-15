package com.linkbucket.linkbucket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FolderRecyclerAdapter extends RecyclerView.Adapter<FolderRecyclerAdapter.FolderViewHolder> {
    ArrayList<FolderRecyclerItem> folderRecyclerItems=new ArrayList<>();
    private OnFolderSelectedListener mListener;
    private OnFolderClickedListener  clickListener;
    final CollectionActivity collectionActivity;

    public FolderRecyclerAdapter(ArrayList<FolderRecyclerItem> folderRecyclerItems) {
        this.folderRecyclerItems = folderRecyclerItems;
        collectionActivity=null;

    }

    public FolderRecyclerAdapter(ArrayList<FolderRecyclerItem> folderRecyclerItems,CollectionActivity collectionActivity) {
        this.folderRecyclerItems = folderRecyclerItems;
        this.collectionActivity=collectionActivity;
    }

    public void setOnCardSelectedListener(OnFolderSelectedListener listener){
        mListener=listener;
    }
    public interface OnFolderSelectedListener{
        void onFolderSelected(String folderName);
    }
    public void setOnFolderClickedListener(OnFolderClickedListener listener){
        clickListener=listener;
    }

    public interface OnFolderClickedListener{
        void onFolderClicked(String folderName);
    }
    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View v = layoutInflater.inflate(R.layout.common_collection_layout, parent, false);
        return new FolderViewHolder(v,mListener,clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        FolderRecyclerItem folderRecyclerItem=folderRecyclerItems.get(position);
        if(folderRecyclerItem.getState())
        {
            holder.v.setAlpha((float) 0.3);
        }
        else
        {
            holder.v.setAlpha(1);
        }
        holder.folderName.setText(folderRecyclerItem.getFolderName());
    }

    @Override
    public int getItemCount() {
        return folderRecyclerItems.size();
    }

    public class FolderViewHolder extends RecyclerView.ViewHolder{
        final TextView folderName;
        final View v;
        public FolderViewHolder(@NonNull View itemView,OnFolderSelectedListener listener,OnFolderClickedListener clickedListener) {
            super(itemView);
            v=itemView;
            folderName=itemView.findViewById(R.id.folderName);
            if(collectionActivity!=null){
                itemView.setOnLongClickListener(v -> {
                    if(folderRecyclerItems.get(getAdapterPosition()).getFolderName() == folderName.getText()){
                        if(itemView.getAlpha()==(float)0.3){
                            itemView.setAlpha(1);
                            folderRecyclerItems.get(getAdapterPosition()).changeState(false);
                            collectionActivity.changeSelectedCount("minus");
                        }
                        else {
                            itemView.setAlpha((float) 0.3);
                            folderRecyclerItems.get(getAdapterPosition()).changeState(true);
                            collectionActivity.changeSelectedCount("plus");
                        }
                        listener.onFolderSelected(folderRecyclerItems.get(getAdapterPosition()).getFolderName());
                    }
                    return true;
                });
            }
            itemView.setOnClickListener(v -> {
                clickedListener.onFolderClicked(folderRecyclerItems.get(getAdapterPosition()).getFolderName());
            });

        }
    }

    public void filterNewList(ArrayList<FolderRecyclerItem> newFolderRecyclerItems){
        folderRecyclerItems=newFolderRecyclerItems;
        notifyDataSetChanged();
    }
}

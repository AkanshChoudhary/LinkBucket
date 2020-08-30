package com.example.linkbucket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CollectionAndFriendRecyclerAdapter extends RecyclerView.Adapter<CollectionAndFriendRecyclerAdapter.ViewHolder> {
    private ArrayList<CollectionAndFriendRecyclerItem> collectionAndFriendRecyclerItemArrayList;
    OnFolderClickedListener onFolderClickedListener;
    public CollectionAndFriendRecyclerAdapter(ArrayList<CollectionAndFriendRecyclerItem> collectionAndFriendRecyclerItemArrayList)
    {
        this.collectionAndFriendRecyclerItemArrayList=collectionAndFriendRecyclerItemArrayList;
    }

    public void setOnFolderClickedListener(OnFolderClickedListener givenListener)
    {this.onFolderClickedListener=givenListener;}

    public interface OnFolderClickedListener{
        void onFolderClicked(String folderName,String userId2);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View v = layoutInflater.inflate(R.layout.collection_and_friend_name_item, parent, false);
        return new ViewHolder(v, onFolderClickedListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionAndFriendRecyclerItem collectionAndFriendRecyclerItem=collectionAndFriendRecyclerItemArrayList.get(position);
        holder.logo.setImageResource(collectionAndFriendRecyclerItem.getLogo());
        holder.name.setText(collectionAndFriendRecyclerItem.getTitle());
    }

    @Override
    public int getItemCount() {
        return collectionAndFriendRecyclerItemArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        final ImageView logo;
        final TextView name;
        public ViewHolder(@NonNull View itemView,OnFolderClickedListener listener) {
            super(itemView);

            logo=itemView.findViewById(R.id.rowIcon);
            name=itemView.findViewById(R.id.rowName);
            itemView.setOnClickListener(v -> {
                listener.onFolderClicked(name.getText().toString(),collectionAndFriendRecyclerItemArrayList.get(getAdapterPosition()).getUserId());
            });
        }
    }

    public void changeList(ArrayList<CollectionAndFriendRecyclerItem> newList)
    {
        collectionAndFriendRecyclerItemArrayList=newList;
        notifyDataSetChanged();
    }

}

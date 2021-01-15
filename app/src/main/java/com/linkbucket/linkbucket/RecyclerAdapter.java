package com.linkbucket.linkbucket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayList<RecyclerItem> recyclerItemList;
    private OnCardSelectedListener mListener;
    private MainUserDashboard mainUserDashboard;
    private CardsOfCollection cardsOfCollection;
    private OnCardClickedListener listener2;
    final int flag;
    private OnMoreClickedListener listener3;
    public RecyclerAdapter(ArrayList<RecyclerItem> recyclerItemList) {
        this.recyclerItemList = recyclerItemList;
        flag=0;
    }

    public RecyclerAdapter(ArrayList<RecyclerItem> recyclerItemList, MainUserDashboard mainUserDashboard) {
        this.recyclerItemList = recyclerItemList;
        this.mainUserDashboard = mainUserDashboard;
        this.flag = 0;
    }

    public RecyclerAdapter(ArrayList<RecyclerItem> recyclerItemList, CardsOfCollection cardsOfCollection) {
        this.recyclerItemList = recyclerItemList;
        this.cardsOfCollection = cardsOfCollection;
        this.flag = 1;
    }

    public void setOnCardSelectedListener(OnCardSelectedListener listener) {
        mListener = listener;
    }

    public void setOnMoreClickedListener(OnMoreClickedListener listener3)
    {
        this.listener3=listener3 ;
    }

    public interface OnMoreClickedListener{
        void onMoreClicked(RecyclerItem recyclerItem);
    }

    public void setOnCardClickedListener(OnCardClickedListener listener2) {
        this.listener2 = listener2;
    }

    public interface OnCardClickedListener {
        void onCardClicked(String link);
    }

    public interface OnCardSelectedListener {
        void onCardSelected(String cardNumber);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View v = layoutInflater.inflate(R.layout.common_card_layout, parent, false);
        return new ViewHolder(v, mListener, listener2,listener3);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecyclerItem recyclerItem = recyclerItemList.get(position);
        holder.icon.setImageResource(recyclerItem.getCardIcon());
        holder.title.setText(recyclerItem.getCardName());
        holder.desc.setText(recyclerItem.getCardDesc());
        holder.link = recyclerItem.getLink();
        if(recyclerItem.isMoreNeeded()){holder.moreBtn.setVisibility(View.VISIBLE);}
        else if(!recyclerItem.isMoreNeeded()){holder.moreBtn.setVisibility(View.GONE);}
        if (recyclerItem.getSelected()) {
            holder.v.setAlpha((float) 0.3);
        } else {
            holder.v.setAlpha(1);
        }
    }

    @Override
    public int getItemCount() {
        return recyclerItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView title;
        final TextView desc;
        String link;
        final Button moreBtn;
        final View v;
        RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView, OnCardSelectedListener listener, OnCardClickedListener listener2,OnMoreClickedListener listener3) {
            super(itemView);
            icon = itemView.findViewById(R.id.cardIcon);
            title = itemView.findViewById(R.id.cardName);
            desc = itemView.findViewById(R.id.cardDesc);
            moreBtn = itemView.findViewById(R.id.more);
            v = itemView;
            itemView.setOnLongClickListener(v -> {
                if(title.getText()==recyclerItemList.get(getAdapterPosition()).getCardName()){
                    if (itemView.getAlpha() == (float) 0.3) {
                        itemView.setAlpha(1);
                        recyclerItemList.get(getAdapterPosition()).changeSelected(false);
                        if (flag == 0) {
                            mainUserDashboard.changeCount("minus");
                        } else {
                            cardsOfCollection.changeCount("minus");
                        }
                    } else {
                        itemView.setAlpha((float) 0.3);
                        recyclerItemList.get(getAdapterPosition()).changeSelected(true);
                        if (flag == 0) {
                            mainUserDashboard.changeCount("plus");
                        } else {
                            cardsOfCollection.changeCount("plus");
                        }
                    }
                    listener.onCardSelected(recyclerItemList.get(getAdapterPosition()).getCardNumber());
                }
                return true;
            });

            itemView.setOnClickListener(v -> {
                listener2.onCardClicked(link);
            });

            moreBtn.setOnClickListener(v1 -> {
                listener3.onMoreClicked(recyclerItemList.get(getAdapterPosition()));
            });

        }
    }

    public void filterNewList(ArrayList<RecyclerItem> newList) {
        recyclerItemList = newList;
        notifyDataSetChanged();
    }
}

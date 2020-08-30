package com.example.linkbucket;

import android.content.Context;

import java.io.Serializable;

public class RecyclerItem implements Serializable {
    private final int cardIcon;
    private  String cardName;
    private  String cardDesc;
    private  String link;
    final Context context;
    boolean state;
    int flag;
    long cardNumber;
    boolean isMoreNeeded;

    public RecyclerItem(int cardIcon, String cardName, String cardDesc, String link, Context context,long cardNumber,boolean isMoreNeeded) {
        this.cardIcon = cardIcon;
        this.cardName = cardName;
        this.cardDesc = cardDesc;
        this.link = link;
        this.flag=0;
        this.context = context;
        this.cardNumber=cardNumber;
        this.isMoreNeeded=isMoreNeeded;
    }

    public long getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(int cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public void setCardDesc(String cardDesc) {
        this.cardDesc = cardDesc;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getCardIcon() {
        return cardIcon;
    }

    public String getCardName() {
        return cardName;
    }

    public String getCardDesc() {
        return cardDesc;
    }

    public String getLink() {
        return link;
    }

    public void changeState(boolean state) {
        this.state = state;
    }

    public boolean getState() {
        return state;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public boolean isMoreNeeded() {
        return isMoreNeeded;
    }
}

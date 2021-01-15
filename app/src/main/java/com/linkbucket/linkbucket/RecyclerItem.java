package com.linkbucket.linkbucket;

import android.content.Context;

import java.io.Serializable;

public class RecyclerItem implements Serializable {
    private final int cardIcon;
    private  String cardName;
    private  String cardDesc;
    private  String link;
    final Context context;
    boolean selected;
    int flag;
    String id;
    final boolean isMoreNeeded;
    public RecyclerItem(int cardIcon, String cardName, String cardDesc, String link, Context context,String id,boolean isMoreNeeded,boolean selected) {
        this.cardIcon = cardIcon;
        this.cardName = cardName;
        this.cardDesc = cardDesc;
        this.link = link;
        this.flag=0;
        this.context = context;
        this.id=id;
        this.isMoreNeeded=isMoreNeeded;
        this.selected=selected;
    }

    public String getCardNumber() {
        return this.id;
    }

    public void setCardNumber(String id) {
        this.id = id;
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

    public void changeSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean getSelected() {
        return selected;
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

package com.example.linkbucket;

public class HistoryItem {
    String bunch_name;
    String info;
    long statusCode;
    boolean isLink;

    public HistoryItem(String bunch_name, String info,boolean isLink) {
        this.bunch_name = bunch_name;
        this.info = info;
        this.statusCode=1;
        this.isLink=isLink;
    }

    public HistoryItem(String bunch_name, String info, long statusCode) {
        this.bunch_name = bunch_name;
        this.info = info;
        this.statusCode = statusCode;
    }

    public String getBunch_name() {
        return bunch_name;
    }

    public String getInfo() {
        return info;
    }

    public long getStatusCode() {
        return statusCode;
    }

    public boolean isLink() {
        return isLink;
    }
}

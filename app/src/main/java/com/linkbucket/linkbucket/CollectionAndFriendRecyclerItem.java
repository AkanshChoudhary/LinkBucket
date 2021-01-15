package com.linkbucket.linkbucket;

public class CollectionAndFriendRecyclerItem {
    private final int logo;
    private final String title;
    private final String userId;


    public CollectionAndFriendRecyclerItem(int logo,String title,String userId)
    {
        this.logo=logo;
        this.title=title;
        this.userId=userId;
    }

    public String getUserId() {
        return userId;
    }

    public int getLogo() {
        return logo;
    }

    public String getTitle() {
        return title;
    }
}

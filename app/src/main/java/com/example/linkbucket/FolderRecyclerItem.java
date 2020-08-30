package com.example.linkbucket;

public class FolderRecyclerItem {
    private String folderName;
    public boolean selectedState;
    int flag;

    public FolderRecyclerItem(String folderName) {
        this.folderName = folderName;
        this.flag=0;
    }

    public void changeName(String name){
        folderName=name;
    }

    public String getFolderName() {
        return folderName;
    }

    public void changeState(boolean state){
        this.selectedState=state;
    }
    public boolean getState(){
        return selectedState;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}

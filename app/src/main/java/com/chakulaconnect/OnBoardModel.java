package com.chakulaconnect;

public class OnBoardModel {
    String contentText;
    int imageId;

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public OnBoardModel(String contentText, int imageId) {
        this.contentText = contentText;
        this.imageId = imageId;
    }
}

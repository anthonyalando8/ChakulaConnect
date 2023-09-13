package com.chakulaconnect;

import java.util.HashMap;

public class NotificationModel {
    String notifyDate;
    String notifyBody;
    String notifyId;
    String notifyDescription;
    String imageUri;
    HashMap<String, String> flags;
    String notifySource, notifyLink;

    public String getNotifySource() {
        return notifySource;
    }

    public void setNotifySource(String notifySource) {
        this.notifySource = notifySource;
    }

    public String getNotifyLink() {
        return notifyLink;
    }

    public void setNotifyLink(String notifyLink) {
        this.notifyLink = notifyLink;
    }

    public NotificationModel(String notifyDate, String notifyBody, String notifyId,
                             String notifyDescription, String imageUri, HashMap<String, String> flags, String notifySource, String notifyLink) {
        this.notifyDate = notifyDate;
        this.notifyBody = notifyBody;
        this.notifyId = notifyId;
        this.notifyDescription = notifyDescription;
        this.imageUri = imageUri;
        this.flags = flags;
        this.notifySource = notifySource;
        this.notifyLink = notifyLink;
    }
    public  NotificationModel(){
        //Required empty constructor
    }

    public String getNotifyDate() {
        return notifyDate;
    }

    public void setNotifyDate(String notifyDate) {
        this.notifyDate = notifyDate;
    }

    public String getNotifyBody() {
        return notifyBody;
    }

    public void setNotifyBody(String notifyBody) {
        this.notifyBody = notifyBody;
    }

    public String getNotifyId() {
        return notifyId;
    }

    public void setNotifyId(String notifyId) {
        this.notifyId = notifyId;
    }

    public String getNotifyDescription() {
        return notifyDescription;
    }

    public void setNotifyDescription(String notifyDescription) {
        this.notifyDescription = notifyDescription;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public HashMap<String, String> getFlags() {
        return flags;
    }

    public void setFlags(HashMap<String, String> flags) {
        this.flags = flags;
    }
}

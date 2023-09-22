package com.chakulaconnect;

import java.util.HashMap;

public class PostModel {
    String postReference, time, urlLink, textDescription, sourceId, postId, reference;
    int postType;
    HashMap<String, String> imageUri;
    HashMap<String, String> flags;

    public PostModel(String postReference, String time, String urlLink,
                     String textDescription, String sourceId, int postType, HashMap<String, String> imageUri,
                     HashMap<String, String> flags, String postId, String reference) {
        this.postReference = postReference;
        this.time = time;
        this.urlLink = urlLink;
        this.textDescription = textDescription;
        this.sourceId = sourceId;
        this.postType = postType;
        this.imageUri = imageUri;
        this.flags = flags;
    }
    public PostModel(){}
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPostReference() {
        return postReference;
    }

    public void setPostReference(String postReference) {
        this.postReference = postReference;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUrlLink() {
        return urlLink;
    }

    public void setUrlLink(String urlLink) {
        this.urlLink = urlLink;
    }

    public String getTextDescription() {
        return textDescription;
    }

    public void setTextDescription(String textDescription) {
        this.textDescription = textDescription;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public int getPostType() {
        return postType;
    }

    public void setPostType(int postType) {
        this.postType = postType;
    }

    public HashMap<String, String> getImageUri() {
        return imageUri;
    }

    public void setImageUri(HashMap<String, String> imageUri) {
        this.imageUri = imageUri;
    }

    public HashMap<String, String> getFlags() {
        return flags;
    }

    public void setFlags(HashMap<String, String> flags) {
        this.flags = flags;
    }
}

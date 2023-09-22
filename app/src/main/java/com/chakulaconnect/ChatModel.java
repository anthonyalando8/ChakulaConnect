package com.chakulaconnect;

public class ChatModel {
    String sender, receiver, time, status, message, reference;

    public ChatModel(String sender, String receiver, String time, String status, String message, String reference) {
        this.sender = sender;
        this.receiver = receiver;
        this.time = time;
        this.status = status;
        this.message = message;
        this.reference = reference;
    }

    public ChatModel(){
        //Required empty
    }
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}

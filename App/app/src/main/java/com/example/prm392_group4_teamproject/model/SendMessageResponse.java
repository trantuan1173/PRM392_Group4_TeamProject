package com.example.prm392_group4_teamproject.model;
public class SendMessageResponse {
    private String message;
    private Message data;

    public SendMessageResponse(String message, Message data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message getData() {
        return data;
    }

    public void setData(Message data) {
        this.data = data;
    }
}
package com.example.prm392_group4_teamproject;

public class MessageSendRequest {
    private String content;
    private String messageType = "text";

    public MessageSendRequest(String content, String messageType) {
        this.content = content;
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
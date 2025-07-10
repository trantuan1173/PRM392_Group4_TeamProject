package com.example.prm392_group4_teamproject.model;

import java.util.List;

public class MessageListResponse {
    private List<Message> messages;
    private Pagination pagination;

    // ... getter/setter


    public MessageListResponse(List<Message> messages, Pagination pagination) {
        this.messages = messages;
        this.pagination = pagination;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
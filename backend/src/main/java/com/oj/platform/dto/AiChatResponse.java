package com.oj.platform.dto;

import java.time.LocalDateTime;

public class AiChatResponse {

    private String reply;
    private String status;
    private LocalDateTime timestamp;

    public AiChatResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public AiChatResponse(String reply, String status) {
        this.reply = reply;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

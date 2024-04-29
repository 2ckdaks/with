package com.with.with.chat;

public class ChatMessage {
    private MessageType type; // 메시지 타입 (CHAT, JOIN, LEAVE 등)
    private String content; // 메시지 내용
    private String sender; // 메시지 보낸 사람

    // 메시지 타입을 정의하는 enum
    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

    // getters and setters
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}

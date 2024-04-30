package com.with.with.chat;

public class DisconnectRequest {
    private String roomId;
    private String username;

    // 기본 생성자
    public DisconnectRequest() {}

    // 필드를 포함한 생성자
    public DisconnectRequest(String roomId, String username) {
        this.roomId = roomId;
        this.username = username;
    }

    // Getter 및 Setter 메서드
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

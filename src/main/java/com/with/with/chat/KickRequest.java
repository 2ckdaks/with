package com.with.with.chat;

public class KickRequest {
    private String usernameToKick;
    private String admin;

    // 게터와 세터 추가
    public String getUsernameToKick() {
        return usernameToKick;
    }

    public void setUsernameToKick(String usernameToKick) {
        this.usernameToKick = usernameToKick;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
}


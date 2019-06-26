package com.github.tmbst;

public class Player {

    /* Attributes */
    private String username;
    private SessionState.Roles role;
    private Boolean isAlive;

    public Player(String username, SessionState.Roles role) {
        this.username = username;
        this.role = role;
        this.isAlive = true;
    }

    public String getUsername() {
        return username;
    }

    public SessionState.Roles getRole() {
        return role;
    }

    public Boolean getStatus() {
        return isAlive;
    }

    public void setRole(SessionState.Roles role) {
        this.role = role;
    }

    public void setStatus(Boolean status) {
        this.isAlive = status;
    }
}

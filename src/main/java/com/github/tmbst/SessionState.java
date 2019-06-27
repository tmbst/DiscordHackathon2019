package com.github.tmbst;

public class SessionState {

    // Roles stays the same so I made it public and static
    public static enum Roles {CITIZEN, MAFIA}

    private Boolean isDay;

    // Constructor
    public SessionState() {
        this.isDay = true;
    }

    // Change the phase
    public void changePhase() {
        this.isDay = !this.isDay;
    }

    // Get current time
    public Boolean isDay() {
        return isDay;
    }
}

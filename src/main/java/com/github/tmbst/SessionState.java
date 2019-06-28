package com.github.tmbst;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;

import java.util.ArrayList;
import java.util.List;

public class SessionState {

    // Roles stays the same so I made it public and static
    public static enum Roles {MAFIA, CITIZEN};

    private Boolean isDay;
    private Roles activeRole;
    private ServerTextChannel townChannel;
    private ServerTextChannel mafiaChannel;
    private List<Player> playerList;
    private Server server;

    // Constructor
    public SessionState() {
        this.isDay = true;
        this.activeRole = Roles.CITIZEN;
        playerList = new ArrayList<>();
    }

    public void toggleDay() {
        this.isDay = !this.isDay;
    }

    // Get current time
    public Boolean isDay() {
        return isDay;
    }

    public Roles getActiveRole() {
        return activeRole;
    }

    public void setActiveRole(Roles newRole) {
        this.activeRole = newRole;
    }

    public ServerTextChannel getTownChannel() {
        return townChannel;
    }

    public void setTownChannel(ServerTextChannel channel) {
        this.townChannel = channel;
    }

    public ServerTextChannel getMafiaChannel() {
        return mafiaChannel;
    }

    public void setMafiaChannel(ServerTextChannel channel) {
        this.mafiaChannel = channel;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public void addPlayer(Player p) {
        playerList.add(p);
    }

    public void removePlayer(Player p) {
        playerList.remove(p);
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}

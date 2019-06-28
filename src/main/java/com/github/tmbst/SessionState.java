package com.github.tmbst;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.ArrayList;
import java.util.List;

public class SessionState {

    // Roles stays the same so I made it public and static
    public static enum Roles {MAFIA, CITIZEN};

    private Boolean isDay;
    private Roles activeRole;
    private ServerTextChannel townChannel;
    private ServerTextChannel mafiaChannel;
    private ServerTextChannel graveyardChannel;
    private List<Player> playerList;
    private List<User> mafiaList;
    private List<User> citizenList;
    private List<User> usersList;
    private Server server;
    private Role deadRole;
    private Role aliveRole;
    private int numPlayers;

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

    public void setDeadRole(Role deadRole) {this.deadRole = deadRole;}

    public void setAliveRole(Role aliveRole) {this.aliveRole = aliveRole;}

    public void setNumPlayers(int numPlayers) {this.numPlayers = numPlayers;}

    public int getNumPlayers() {
        return numPlayers;
    }

    public Role getDeadRole() {return deadRole;}

    public Role getAliveRole() {return aliveRole;}

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

    public ServerTextChannel getGraveyardChannel() {
        return graveyardChannel;
    }

    public void setGraveyardChannel(ServerTextChannel channel) {
        this.graveyardChannel = channel;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }
    public List<User> getMafiaList() {
        return mafiaList;
    }

    public List<User> getCitizenList() {
        return citizenList;
    }

    public List<User> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<User> usersList) {
        this.usersList = usersList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public void setMafiaList(List<User> mafiaList) {this.mafiaList = mafiaList;}

    public void setCitizenList(List<User> citizenList) {this.citizenList = citizenList;}

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

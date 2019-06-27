package com.github.tmbst;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

// Singleton class

public class Game {

    // Attributes <TODO add more attributes when necessary>
    private static Game game;
    private DiscordApi api;

    // Constructor
    private Game(DiscordApi api) {
        this.api = api;
    }

    // Starts and sets up the game
    public static void startGame(DiscordApi api) {
        game = new Game(api);
    }

    // Get singleton
    public static Game getGame(DiscordApi api) {
        return game;
    }

}

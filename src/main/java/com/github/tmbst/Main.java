package com.github.tmbst;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class Main {

    public static void main(String[] args) {

        String token = System.getenv("DISCORDTOKEN");
        if (token == null) {
            System.out.println("Error! Discord token not set. Exiting...");
            System.exit(1);
        }
        
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        // Add a listener which answers with "Pong!" if someone writes "!ping"
        api.addListener(new PingCommand());

        Game.startGame(api);

        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }
}


package com.github.tmbst;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class Main {

    public static void main(String[] args) {
        // Insert your bot's token here
        if (args.length < 1) {
            System.out.println("Please enter the bot's token as an argument.");
            System.exit(1);
        }
        String token = args[0];

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        // Add a listener which answers with "Pong!" if someone writes "!ping"
        api.addListener(new PingCommand());

        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }

}


package com.github.tmbst;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.io.File;


public class Session implements MessageCreateListener {

    private static int playerCounter = 0;
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        System.out.println(event.getMessage());
        if (event.getMessage().getContent().equalsIgnoreCase("!join")) {
            // Create the embed
            EmbedBuilder joinEmbed = new EmbedBuilder()
                    .setTitle("Starting: Town of Discord!")
                    .addField("Players needed to play:", "5")
                    .setDescription("React to join!")
                    .setAuthor(event.getMessageAuthor().getDisplayName(), null, event.getMessageAuthor().getAvatar())
                    .setColor(Color.BLUE)
                    .setImage(new File("/home/duckytape/Projects/discordHackathon/src/main/java/com/github/tmbst/resources/splashArt.jpg"))
                    .setFooter("Discord Hack Week 2019 Submission!");

            // Set up the Message to be sent
            Message message = event.getChannel().sendMessage(joinEmbed).join();
            message.addReaction("\uD83D\uDC4D");

            // Begin listening for :thumbs-up: reacts, 5 needed
            message.addReactionAddListener(emojiEvent -> {
                if (emojiEvent.getEmoji().equalsEmoji("\uD83D\uDC4D")) {
                    playerCounter++;
                    // Close listener, start the game.
                    if (playerCounter == 5) {

                    }
                }
            });

        }

    }
}

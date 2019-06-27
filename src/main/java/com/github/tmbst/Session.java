package com.github.tmbst;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.event.ListenerManager;

import java.awt.*;
import java.io.File;
import java.util.Optional;


public class Session implements MessageCreateListener {

    private static int playerCounter = 0;
    private static ListenerManager<ReactionAddListener> emojiAddListenerMgr;
    private static ListenerManager<ReactionRemoveListener> emojiRmvListenerMgr;

    @Override
    public void onMessageCreate(MessageCreateEvent event) {

        if (event.getMessage().getContent().equalsIgnoreCase("!join")) {

            // Get information from the event
            Optional<Server> server = event.getServer();


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

            // Begin listening for :thumbs-up: reacts, 5 needed, add to a user list
            // TODO: removeOwnReactEmojiFromMessage(),
            emojiAddListenerMgr = message.addReactionAddListener(emojiAddEvent -> {
                if (emojiAddEvent.getEmoji().equalsEmoji("\uD83D\uDC4D")) {

                    playerCounter++;
                    event.getChannel().sendMessage(Integer.toString(playerCounter));

                    // Set-Up Game, listeners closed at this point
                    if (playerCounter == 3) {
                        setUp(server);
                    }
                }
            });
            // Listen for when user's remove their reacts, essentially remove them from the list of users
            emojiRmvListenerMgr = message.addReactionRemoveListener(emojiRmvEvent -> {
                playerCounter--;
                event.getChannel().sendMessage(Integer.toString(playerCounter));
            });
        }

    }

    // Creates the text-channels needed for the game, determines the users playing
    public void setUp(Optional<Server> serv) {
        Server server;

        // Remove any listeners
        emojiAddListenerMgr.remove();
        emojiRmvListenerMgr.remove();
        playerCounter = 0;

        // Check if the server exists.
        if (serv.isPresent()){
            server = serv.get();
            new ServerTextChannelBuilder(server)
                    .setName("townOfDiscord")
                    .create();
        }
    }
}

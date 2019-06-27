package com.github.tmbst;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.event.ListenerManager;

import java.awt.*;
import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;


public class Session implements MessageCreateListener {

    private static ListenerManager<ReactionAddListener> emojiAddListenerMgr;

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
            // TODO: removeOwnReactEmojiFromMessage() OR have an off by one
            emojiAddListenerMgr = message.addReactionAddListener(emojiAddEvent -> {
                if (emojiAddEvent.getEmoji().equalsEmoji("\uD83D\uDC4D")) {

                    // Check the counter
                    if (emojiAddEvent.getCount().isPresent()) {
                        int playerCount = emojiAddEvent.getCount().get();

                        // Debug
                        new MessageBuilder()
                                .append("Added")
                                .append(emojiAddEvent.getUser().getName())
                                .append(Integer.toString(playerCount))
                                .send(event.getChannel());


                        // Set-Up Game, listeners closed at this point
                        if (playerCount == 2) {

                            // Create players
                            ArrayList<Player> playerList = new ArrayList<>();
                            List<User> userList = emojiAddEvent.getUsers().join();

                            try{
                                // For each user wanting to play, create a Player Obj.
                                for (User user : userList) {

                                    // Obtain Username & Assign random role
                                    String name = user.getName();
                                    SessionState.Roles role = SessionState.Roles
                                            .values()[new Random().nextInt(SessionState.Roles.values().length)];

                                    Player player = new Player(name, role);
                                    playerList.add(player);
                                }
                            } catch (Exception e) {
                                System.out.println("Error: " + e);
                            }

                            // Everything ready for Set-Up!
                            setUp(server, playerList);
                        }
                    }
                }
            });

        }

    }

    // Creates the text-channels needed for the game, determines the users playing
    public void setUp(Optional<Server> serv, ArrayList<Player> players) {
        Server server;
        // Remove any listeners
        emojiAddListenerMgr.remove();

        // Check if the server exists.
        if (serv.isPresent()){
            server = serv.get();

            // Set up the main text-channel
            ServerTextChannel TODTextChan =  new ServerTextChannelBuilder(server)
                    .setName("townOfDiscord")
                    .setTopic("Welcome all to the Town of Discord!")
                    .create()
                    .join();


            for (Player p : players) {
               TODTextChan.sendMessage("Welcome: " + p.getUsername() + " " + p.getRole());
            }



        }
    }
}

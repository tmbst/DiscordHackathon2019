package com.github.tmbst;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.channel.ServerTextChannelUpdater;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
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
            // TODO: Add our TMBST Org logo as a thumbnail
            // TODO: Add our finished "Town of Discord" logo to the setImage
            // TODO: Hardcoded image! look at .setImage() (Clearly ducky's files)
            EmbedBuilder joinEmbed = new EmbedBuilder()
                    .setTitle("Starting: Town of Discord!")
                    .addField("Players needed to play:", "5")
                    .setDescription("React to join!")
                    .setAuthor(event.getMessageAuthor().getDisplayName(), null, event.getMessageAuthor().getAvatar())
                    .setColor(Color.BLUE)
                    .setImage(new File("/home/duckytape/Projects/discordHackathon/src/main/java/com/github/tmbst/resources/splashArt.jpg"))
                    .setFooter("Discord Hack Week 2019 Submission!");

            // Set up the Message to be sent, initially thumbs-up react this message
            Message message = event.getChannel().sendMessage(joinEmbed).join();
            message.addReaction("\uD83D\uDC4D");

            // Begin listening for :thumbs-up: reacts, 5 needed, add to a user list
            // TODO: removeOwnReactEmojiFromMessage() OR have an off by one
            // TODO: For testing sake, limit set to 2
            emojiAddListenerMgr = message.addReactionAddListener(emojiAddEvent -> {
                if (emojiAddEvent.getEmoji().equalsEmoji("\uD83D\uDC4D")) {
                    // Check the counter
                    if (emojiAddEvent.getCount().isPresent()) {
                        int playerCount = emojiAddEvent.getCount().get();

                        // Debug
                        // TODO: Removed when product is finished
                        new MessageBuilder()
                                .append("Added")
                                .append(emojiAddEvent.getUser().getName())
                                .append(Integer.toString(playerCount))
                                .send(event.getChannel());


                        // Set-Up Game, listeners will close as soon as setUp is called.
                        if (playerCount == 2) {
                            // Get the list of users wanting to play, pass to the set-up
                            List<User> userList = emojiAddEvent.getUsers().join();
                            setUp(server,  userList);
                        }
                    }
                }
            });

        }

    }

    // FUNCTION: setUp
    // DESCRIPTION: Creates the text-channels needed for the game, determines the users playing
    public void setUp(Optional<Server> serv, List<User> users) {

        Server server;                                      // The server, needed in order to create text-channels
        ArrayList<Player> players = new ArrayList<>();      // Player Obj Lists
        ArrayList<User> userMafiaList = new ArrayList<>();  // List of Users in the Mafia

        // Remove any listeners
        emojiAddListenerMgr.remove();

        // TODO: Note that a cap is needed on number of Mafia! Minimum of at least 1 for 5 players...
        // For each user wanting to play, create a Player Obj.
        for (User user : users) {

            // Obtain Username & Assign random role
            String name = user.getName();
            SessionState.Roles role = SessionState.Roles
                    .values()[new Random().nextInt(SessionState.Roles.values().length)];

            // This is to distinguish who is mafia for when setting up the mafia text-channel
            if(role == SessionState.Roles.MAFIA){
                userMafiaList.add(user);
            }

            // Create player and add to list of players
            Player player = new Player(name, role);
            players.add(player);

        }

        // Check if the server exists.
        if (serv.isPresent()){
            server = serv.get();

            // Set up the main text-channel
            // DENY: Everyone from seeing the channel except the players playing the game.
            // ALLOW: Players playing to see the #townofdiscord channel.
            ServerTextChannelBuilder TODTextChanBuilder = new ServerTextChannelBuilder(server)
                    .setName("townOfDiscord")
                    .setTopic("Welcome all to the Town of Discord!")
                    .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder()
                            .setDenied(PermissionType.READ_MESSAGES, PermissionType.SEND_MESSAGES)
                            .build());
            for (User u : users) {
                TODTextChanBuilder.addPermissionOverwrite(u, new PermissionsBuilder()
                            .setAllowed(PermissionType.READ_MESSAGES, PermissionType.SEND_MESSAGES, PermissionType.ADD_REACTIONS)
                            .setDenied(PermissionType.ATTACH_FILE)
                            .build());
            }
            // Create the #townOfDiscord text channel
            ServerTextChannel TODTextChan = TODTextChanBuilder.create().join();

            // Welcome message for the #townOfDiscord channel
            for (Player p : players) {
               TODTextChan.sendMessage("Welcome: " + p.getUsername() + " " + p.getRole());
            }

            // TODO: Note that as long as we assign at least 1 mafia, then this check is irrelevant! Get rid of later.
            if (!userMafiaList.isEmpty()) {

                // Set up the mafia text-channel
                // DENY: All players except mafia from seeing this channel.
                ServerTextChannelBuilder mafiaTextChannelBuilder = new ServerTextChannelBuilder(server)
                        .setName("mafia-hideout")
                        .setTopic("Welcome Mafia, try to kill off all the citizens!")
                        .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder()
                                .setDenied(PermissionType.READ_MESSAGES, PermissionType.SEND_MESSAGES)
                                .build());

                for (User u : userMafiaList) {
                    mafiaTextChannelBuilder.addPermissionOverwrite(u, new PermissionsBuilder()
                            .setAllowed(PermissionType.READ_MESSAGES, PermissionType.SEND_MESSAGES, PermissionType.ADD_REACTIONS)
                            .setDenied(PermissionType.ATTACH_FILE)
                            .build());
                }
                // Create the #mafiahideout text channel
                ServerTextChannel mafiaTextChan = mafiaTextChannelBuilder.create().join();

                // Welcome message for the #mafiahideout channel
                mafiaTextChan.sendMessage("@here Welcome to the Hideout! Your task is to kill all citizens." +
                        "During the night you can choose a user to kill.");

            }

        }
    }
}

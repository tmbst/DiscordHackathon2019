package com.github.tmbst;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.RoleBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.util.event.ListenerManager;

import java.awt.*;
import java.util.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.*;


public class Session implements MessageCreateListener {

    private static ListenerManager<ReactionAddListener> emojiAddListenerMgr;
    public SessionState state;
    private static final int DAYLENGTH = 1;
    private static final int PLAYERSPERMAFIA = 2; // Change this to 2 to test Mafia for 2 Players
    private static final int MINPLAYERS = 2;

    @Override
    public void onMessageCreate(MessageCreateEvent event) {

        if (event.getMessage().getContent().equalsIgnoreCase("!join")) {

            // Get information from the event
            Optional<Server> server = event.getServer();

            // Create the embed
            EmbedBuilder joinEmbed = new EmbedBuilder()
                    .setTitle("Starting: Town of Discord!")
                    .addField("Players needed to play:", Integer.toString(MINPLAYERS))
                    .setDescription("React to join!")
                    .setAuthor(event.getMessageAuthor().getDisplayName(), null, event.getMessageAuthor().getAvatar())
                    .setColor(Color.BLUE)
                    .setThumbnail(new File("resources/icon.png"))
                    .setImage(new File("resources/town-of-discord.png"))
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

                        // Set-Up Game, listeners will close as soon as setUp is called.
                        // +1 because bot doesn't count as a player (since it reacted)
                        if (playerCount == MINPLAYERS+1) {
                            // Get the list of users wanting to play, pass to the set-up
                            emojiAddEvent.removeOwnReactionByEmojiFromMessage("\uD83D\uDC4D").join();
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

        Server server;                                          // The server, needed in order to create text-channels
        ArrayList<Player> players = new ArrayList<>();          // Player Obj Lists
        ArrayList<User> userMafiaList = new ArrayList<>();      // List of Users in the Mafia
        ArrayList<User> userCitizenList = new ArrayList<>();    // List of Users in the Citizens

        state = new SessionState();
        state.setPlayerList(players);
        int mafiaLeft = users.size() / PLAYERSPERMAFIA;
        int usersLeft = users.size();

        state = new SessionState();
        state.setMafiaList(userMafiaList);
        state.setCitizenList(userCitizenList);
        state.setPlayerList(players);
        state.setUsersList(users);
        state.setNumPlayers(users.size());

        // Remove any listeners
        emojiAddListenerMgr.remove();

        // TODO: Note that a cap is needed on number of Mafia! Minimum of at least 1 for 5 players...
        // For each user wanting to play, create a Player Obj.
        Random rand = new Random();
        for (User user : users) {
            SessionState.Roles currRole;
            String currName = user.getName();

            //ensure number of mafia is filled
            boolean isMafia = rand.nextInt(usersLeft) < mafiaLeft;
            if (isMafia) {
                currRole = SessionState.Roles.MAFIA;
                userMafiaList.add(user);
                mafiaLeft--;
            } else {
                //note: this works due to mafia being the first on the list of roles
                currRole = SessionState.Roles.values()[rand.nextInt(SessionState.Roles.values().length - 1) + 1];
                userCitizenList.add(user);
            }
            usersLeft--;

            // Create player and add to list of players
            Player player = new Player(currName, currRole);
            players.add(player);

        }

        // Check if the server exists.
        if (serv.isPresent()){
            server = serv.get();

            state.setServer(server);
            state.setDeadRole(server.getRolesByName("dead").get(0));
            state.setAliveRole(server.getRolesByName("alive").get(0));

            // Everyone is alive at start
            for ( User u : users ) {
                u.removeRole(state.getDeadRole()).join();
                u.addRole(state.getAliveRole()).join();
            }

            // Set up #townofdiscord text-channel
            ServerTextChannelBuilder TODTextChanBuilder = new ServerTextChannelBuilder(server)
                    .setName("townOfDiscord")
                    .setTopic("Welcome all to the Town of Discord!")
                    // @everyone permissions
                    .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder()
                            .setDenied(PermissionType.SEND_MESSAGES)
                            .build())
                    // @dead permissions
                    .addPermissionOverwrite(server.getRolesByName("dead").get(0), new PermissionsBuilder()
                            .setAllowed(PermissionType.READ_MESSAGES)
                            .setDenied(PermissionType.SEND_MESSAGES, PermissionType.ADD_REACTIONS)
                            .build())
                    // @alive permissions
                    .addPermissionOverwrite(server.getRolesByName("alive").get(0), new PermissionsBuilder()
                            .setAllowed(PermissionType.READ_MESSAGES, PermissionType.SEND_MESSAGES, PermissionType.ADD_REACTIONS)
                            .build());
            ServerTextChannel TODTextChan = TODTextChanBuilder.create().join();

            // DEBUG: Welcome message for the #townOfDiscord channel
            // TODO: Don't forget to remove the role reveal..
            for (Player p : players) {
               TODTextChan.sendMessage("Welcome: " + p.getUsername() + " " + p.getRole());
            }

            // Set up #graveyard text-channel
            ServerTextChannelBuilder graveyardTextChanBuilder = new ServerTextChannelBuilder(server)
                    .setName("graveyard")
                    .setTopic("Oh dear, you are dead!")
                    // @everyone permissions
                    .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder()
                            .setDenied(PermissionType.READ_MESSAGES, PermissionType.SEND_MESSAGES)
                            .build())
                    // @dead permissions
                    .addPermissionOverwrite(server.getRolesByName("dead").get(0), new PermissionsBuilder()
                            .setAllowed(PermissionType.READ_MESSAGES, PermissionType.SEND_MESSAGES)
                            .setDenied(PermissionType.ATTACH_FILE)
                            .build());
            ServerTextChannel graveyardTextChan = graveyardTextChanBuilder.create().join();

            // Set up #mafia text-channel
            ServerTextChannelBuilder mafiaTextChannelBuilder = new ServerTextChannelBuilder(server)
                    .setName("mafia-hideout")
                    .setTopic("Welcome Mafia, try to kill off all the citizens!")
                    // @everyone permissions
                    .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder()
                            .setDenied(PermissionType.SEND_MESSAGES)
                            .build());

            // Mafia user permissions
            for (User u : userMafiaList) {
                mafiaTextChannelBuilder.addPermissionOverwrite(u, new PermissionsBuilder()
                        .setAllowed(PermissionType.READ_MESSAGES, PermissionType.SEND_MESSAGES, PermissionType.ADD_REACTIONS)
                        .setDenied(PermissionType.ATTACH_FILE)
                        .build());
            }
            ServerTextChannel mafiaTextChan = mafiaTextChannelBuilder.create().join();
            // Welcome message for Mafia
            mafiaTextChan.sendMessage("@here Welcome to the Hideout! Your task is to kill all citizens." +
                    " During the night you can choose a user to kill.");


            state.setTownChannel(TODTextChan);
            state.setGraveyardChannel(graveyardTextChan);
            state.setMafiaChannel(mafiaTextChan);

            Main.api.addListener(new TearDownCommand(this));

            startDay();

        }

    }

    // TODO: may need a turn for each role. Better to make a role class that has you implement a turn function?
    // THE AGE OLD DEBATE
    public void startDay() {

        //announce day
        EmbedBuilder morningEmbed = new EmbedBuilder()
                .setTitle("Rise and shine!")
                .setDescription("Time for a new day of accusations! Accuse your townsfolk with !suspect <@name>")
                .setImage(new File("resources/day.png"))
                .setColor(Color.BLUE)
                .setFooter("TMBST");
        state.getTownChannel().sendMessage(morningEmbed).join();

        //announce dead
        if (!state.isFirstDay()) {
            User killed = state.getDeadByMafia();
            EmbedBuilder killedEmbed = new EmbedBuilder();
            if (killed != null) {
                killedEmbed.setTitle("Oh dear! One of your fellow townsfolk was found dead this morning!")
                        .setDescription(killed.getName())
                        .setColor(Color.RED)
                        .setThumbnail(killed.getAvatar())
                        .setFooter("You've met with a terrible fate, haven't you?");
                killed.removeRole(state.getAliveRole());
                killed.addRole(state.getDeadRole());
            } else {
                killedEmbed.setTitle("How nice! No one died last night!")
                        .setColor(Color.GREEN)
                        .setFooter("TMBST");
            }
            state.getTownChannel().sendMessage(killedEmbed);
        } else {
            state.toggleFirstDay();
        }

        //ungag everyone
        state.getTownChannel().createUpdater().addPermissionOverwrite(state.getAliveRole(), new PermissionsBuilder()
            .setAllowed(PermissionType.SEND_MESSAGES)
            .build()
        ).update();

        //listen for accusations
        SuspectCommand suspectListener = new SuspectCommand(state.getTownChannel(), state);
        Main.api.addListener(suspectListener);

        //set timer for day
        Session us = this;
        ScheduledExecutorService timerService = Executors.newScheduledThreadPool(1);
        ScheduledFuture morningTimer = timerService.schedule(new Runnable() {
            @Override
            public void run() {

                EmbedBuilder nightEmbed = new EmbedBuilder()
                        .setTitle("The night has arrived. Off to bed!")
                        .setDescription("Please wait for the next day to start.")
                        .setImage(new File("resources/night.png"))
                        .setColor(Color.MAGENTA)
                        .setFooter("Good night everyone :)");
                state.getTownChannel().sendMessage(nightEmbed).join();

                // All alive players cannot talk at night
                state.getTownChannel().createUpdater().addPermissionOverwrite(state.getAliveRole(), new PermissionsBuilder()
                    .setDenied(PermissionType.SEND_MESSAGES)
                    .build()
                ).update();
                Main.api.removeListener(suspectListener);

                Main.api.addListener(new KillCommand(us));
            }
        }, DAYLENGTH, TimeUnit.MINUTES);
    }


}

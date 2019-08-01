package com.github.tmbst;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.util.event.ListenerManager;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;


public class Session implements MessageCreateListener {

    private static ListenerManager<ReactionAddListener> emojiAddListenerMgr;
    public SessionState state;
    private static final int DAYLENGTH = 1;
    private static final int PLAYERSPERMAFIA = 5;
    private static final int MINPLAYERS = 5;
    private static final String THUMBSUP = "\uD83D\uDC4D";

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
                    .setThumbnail(Utility.getResource("icon.png"))
                    .setImage(Utility.getResource("town-of-discord.png"))
                    .setFooter("Discord Hack Week 2019 Submission!");

            // Set up the Message to be sent, initially thumbs-up react this message
            Message message = event.getChannel().sendMessage(joinEmbed).join();
            message.addReaction(THUMBSUP);

            // Begin listening for :thumbs-up: reacts, 5 needed, add to a user list
            emojiAddListenerMgr = message.addReactionAddListener(emojiAddEvent -> {
                if (emojiAddEvent.getEmoji().equalsEmoji(THUMBSUP)) {
                    // Check the counter
                    if (emojiAddEvent.getCount().isPresent()) {
                        int playerCount = emojiAddEvent.getCount().get();

                        // Set-Up Game, listeners will close as soon as setUp is called.
                        // +1 because bot doesn't count as a player (since it reacted)
                        if (playerCount == MINPLAYERS + 1) {
                            // Get the list of users wanting to play, pass to the set-up
                            emojiAddEvent.removeOwnReactionByEmojiFromMessage(THUMBSUP).join();
                            List<User> userList = emojiAddEvent.getUsers().join();
                            setUp(server, userList);
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

        emojiAddListenerMgr.remove();

        /*
            The following loop is a fair method of assigning players the mafia role. Each player has an equal chance at
            receiving the role.
            Each player gets a (mafiaLeft / usersLeft) chance at the role each loop iteration. This chance is devised
            by getting a random number in the range [0, usersLeft - 1] and checking if that number is less than the
            number of mafia spots left.
            This is fair since the accumulated chance of receiving the role at any point in the loop is the product
            (1 - prob of each previous player receiving the role) multiplied by the probability of the current player
            receiving the role, which will always be (numMafia / numPlayers).

            When a player does NOT receive the mafia role, they are given a random role from the role list defined in
            SessionState.Roles that is not mafia. This is done by assuming that the mafia role is the first in the enum
            and grabbing a role that is not the first one. Keep this in mind if changing the enum around.
         */
        Random rand = new Random();
        for (User user : users) {
            SessionState.Roles currRole;
            String currName = user.getName();

            boolean isMafia = rand.nextInt(usersLeft) < mafiaLeft;
            if (isMafia) {
                currRole = SessionState.Roles.MAFIA;
                userMafiaList.add(user);
                mafiaLeft--;
            } else {
                currRole = SessionState.Roles.values()[rand.nextInt(SessionState.Roles.values().length - 1) + 1];
                userCitizenList.add(user);
            }
            usersLeft--;

            Player player = new Player(currName, currRole);
            players.add(player);
        }

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
                .setImage(Utility.getResource("day.png"))
                .setColor(Color.BLUE)
                .setFooter("TMBST");
        state.getTownChannel().sendMessage(morningEmbed).join();

        //announce dead
        if (!state.isFirstDay()) {
            User killed = state.getDeadByMafia();
            EmbedBuilder killedEmbed = new EmbedBuilder();
            if (killed != null) {
                SessionState.Roles killedRole = SessionState.Roles.CITIZEN; //default
                for (Player p : state.getPlayerList()) {
                    if (p.getUsername().equals(killed.getName())) {
                        killedRole = p.getRole();
                    }
                }
                killedEmbed.setTitle("Oh dear! " + killed.getName() + " was found dead this morning!")
                        .setDescription("Their role has been revealed to be...." + killedRole)
                        .setColor(Color.RED)
                        .setThumbnail(killed.getAvatar())
                        .setFooter("You've met with a terrible fate, haven't you?");
                state.getTownChannel().sendMessage(killedEmbed);
                killPlayer(killed);
            } else {
                killedEmbed.setTitle("How nice! No one died last night!")
                        .setColor(Color.GREEN)
                        .setFooter("TMBST");
                state.getTownChannel().sendMessage(killedEmbed);
            }
        } else {
            state.toggleFirstDay();
        }

        //ungag everyone
        state.getTownChannel().createUpdater().addPermissionOverwrite(state.getAliveRole(), new PermissionsBuilder()
            .setAllowed(PermissionType.SEND_MESSAGES)
            .build()
        ).update();


        //listen for accusations
        if (!state.getGameEnded()) {
            SuspectCommand suspectListener = new SuspectCommand(state.getTownChannel(), this);
            Main.api.addListener(suspectListener);

            //set timer for day
            Session us = this;
            ScheduledExecutorService timerService = Executors.newScheduledThreadPool(1);
            ScheduledFuture morningTimer = timerService.schedule(new Runnable() {
                @Override
                public void run() {
                    if (!state.getGameEnded()) {
                        EmbedBuilder nightEmbed = new EmbedBuilder()
                                .setTitle("The night has arrived. Off to bed!")
                                .setDescription("Please wait for the next day to start.")
                                .setImage(Utility.getResource("night.png"))
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
                }
            }, DAYLENGTH, TimeUnit.MINUTES);
        }
    }



    //remove player from their role list, give them the dead role and remove alive role
    //returns true if game ends, false if not, allowing the caller to clean up any listeners
    public void killPlayer(User killed) {
        SessionState.Roles killedRole = SessionState.Roles.CITIZEN; //default
        for (Player p : state.getPlayerList()) {
            if (p.getUsername().equals(killed.getName())) {
                killedRole = p.getRole();
            }
        }
        killed.removeRole(state.getAliveRole());
        killed.addRole(state.getDeadRole());
        if (killedRole == SessionState.Roles.MAFIA) {
            state.getMafiaList().remove(killed);
        } else if (killedRole == SessionState.Roles.CITIZEN) {
            state.getCitizenList().remove(killed);
        }

        //check for end of game
        if (state.getMafiaList().size() == 0) {
            EmbedBuilder citizenWinEmbed = new EmbedBuilder()
                    .setTitle("CITIZENS WIN!");
            state.getTownChannel().sendMessage(citizenWinEmbed).join();
            state.setGameEnded(true);
        } else if (state.getCitizenList().size() == 0) {
            EmbedBuilder mafiaWinEmbed = new EmbedBuilder()
                    .setTitle("THE MAFIA WINS!");
            state.getTownChannel().sendMessage(mafiaWinEmbed).join();
            state.setGameEnded(true);
        }
    }


}

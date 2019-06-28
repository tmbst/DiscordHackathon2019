package com.github.tmbst;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.util.event.ListenerManager;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class SuspectCommand implements MessageCreateListener {

    private static final String commandRegex = "!suspect(\\s+.*)?";
    private static ListenerManager<ReactionAddListener> emojiAddListenerMgr;
    private static final String usage = "Usage: !suspect <@user>";
    private final ServerTextChannel townChannel;
    private static final String hammerEmoji = "\uD83D\uDD28";
    private static final String innocentEmoji = "\uD83D\uDE07";
    private static final String fEmoji = "\uD83C\uDDEB";
    private final SessionState state;

    public SuspectCommand(ServerTextChannel townChannel, SessionState state) {
        super();
        this.townChannel = townChannel;
        this.state = state;

    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {

        final String content = event.getMessageContent();
        String[] callArgs = content.split(" ", 3);

        //TODO: deal with optionals correctly
        if (content.matches(commandRegex) && event.getChannel() == townChannel) {
            Server currServer = event.getServer().get();
            //a @mention in discord is seen as <@xxxxxxxxxxx...> to the bot, where the Xs are the user ID
            //this is also how the bot can mention people
            if (callArgs.length > 1 && callArgs[1].startsWith("<@")) {
                String accusedID = callArgs[1].substring(2, callArgs[1].length() - 1);
                User accusedUser = currServer.getMemberById(accusedID).get();
                String accusedName = accusedUser.getName();
                User accuser = event.getMessage().getAuthor().asUser().get();

                event.getChannel().sendMessage("You have been accused, <@" + accusedID + ">!");

                EmbedBuilder voteEmbed = new EmbedBuilder()
                        .setTitle("Vote now! Should we give " + accusedName + " the banhammer?")
                        .setThumbnail(accusedUser.getAvatar())
                        .setDescription("React to vote! 50% vote required.")
                        .setAuthor(accuser.getName())
                        .setColor(Color.BLUE)
                        .setFooter("TMBST");

                Message voteMessage = event.getChannel().sendMessage(voteEmbed).join();
                voteMessage.addReaction(hammerEmoji);
                voteMessage.addReaction(innocentEmoji);

                int numPlayers = state.getNumPlayers();

                emojiAddListenerMgr = voteMessage.addReactionAddListener(emojiAddEvent -> {
                    // GUILTY
                    if (emojiAddEvent.getEmoji().equalsEmoji(hammerEmoji)) {
                        // VOTE GUILTY COUNTER
                        if (emojiAddEvent.getCount().isPresent()) {
                            int voteCounter = emojiAddEvent.getCount().get();

                            if (voteCounter >= numPlayers) {

                                votePassesGuilty(accusedUser);

                            }
                        }
                     // INNOCENT
                    } else if (emojiAddEvent.getEmoji().equalsEmoji(innocentEmoji)) {
                        // VOTE INNOCENT COUNTER
                        if (emojiAddEvent.getCount().isPresent()) {
                            int voteCounter = emojiAddEvent.getCount().get();

                            if (voteCounter >= numPlayers) {

                                votePassesInnocent(accusedUser);

                            }
                        }

                    }
                });
            } else {
                event.getChannel().sendMessage(usage);
            }
        }
    }

    public void votePassesGuilty(User accusedUser) {
        emojiAddListenerMgr.remove();

        String accusedName = accusedUser.getName();
        List<Player> players = state.getPlayerList();

        EmbedBuilder voteResultEmbed = new EmbedBuilder();
        voteResultEmbed.setTitle(accusedName + " has received the banhammer and is now dead!");
        voteResultEmbed.setAuthor(accusedName);
        voteResultEmbed.setThumbnail(accusedUser.getAvatar());

        // Grab that player's role
        for (Player p: players ) {
            // Get the accused player.
            if(p.getUsername().equals(accusedName)){
                voteResultEmbed.setDescription("Their role has been revealed to be...." + p.getRole());

                if (p.getRole() == SessionState.Roles.MAFIA) {
                    state.getMafiaList().remove(accusedUser);
                }
                else if(p.getRole() == SessionState.Roles.CITIZEN) {
                    state.getCitizenList().remove(accusedUser);
                }
                //state.getUsersList().remove(accusedUser);

                voteResultEmbed.setColor(Color.RED);
                voteResultEmbed.setFooter("F to pay respects");

            }
        }

        // Send message
        Message voteResMessage = state.getTownChannel().sendMessage(voteResultEmbed).join();
        voteResMessage.addReaction(fEmoji);

        // Update user permissions alive -> dead
        accusedUser.removeRole(state.getAliveRole()).join();
        accusedUser.addRole(state.getDeadRole()).join();

    }

    public void votePassesInnocent(User accusedUser) {
        emojiAddListenerMgr.remove();
        String accusedName = accusedUser.getName();
        EmbedBuilder voteResultEmbed = new EmbedBuilder();
        voteResultEmbed.setTitle(accusedName + " is perceived innocent!")
                .setDescription("The Banhammer awaits the next suspect...")
                .setAuthor(accusedName)
                .setThumbnail(accusedUser.getAvatar())
                .setColor(Color.GREEN)
                .setFooter("!suspect still available until night.");
        state.getTownChannel().sendMessage(voteResultEmbed).join();

    }


}

package com.github.tmbst;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class KillCommand implements MessageCreateListener {

    private static final String commandRegex = "!kill(\\s+.*)?";
    private static final String usage = "Usage: !kill <@user>";
    private final Session session;
    private final ServerTextChannel mafiaChannel;
    private final SessionState state;
    private ArrayList<Ballot> victimVotes;
    private static final int VOTETIME = 30;

    private class Ballot {
        User candidate;
        int votes;
        ArrayList<User> voters;

        Ballot(User user) {
            this.candidate = user;
            votes = 1;
            voters = new ArrayList<>(5);
        }
    }


    public KillCommand(Session session) {
        super();
        this.session = session;
        this.state = session.state;
        this.mafiaChannel = state.getMafiaChannel();
        this.victimVotes = new ArrayList<>(5);
        mafiaChannel.sendMessage("Now accepting banhammer votes. Vote with !kill <@user>.");
        startKillTimer();
    }

    /*
    Each call to !kill adds your vote to a specific person. Each person gets one vote. Creating this object
    automatically starts the timer, which will automatically remove the listener and announce the winner of the
    vote.
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        final String content = event.getMessageContent();
        String[] callArgs = content.split(" ", 3);
        //TODO: add more constraints such as player must be alive
        if (content.matches(commandRegex) && event.getChannel() == mafiaChannel) {
            if (callArgs.length > 1 && callArgs[1].startsWith("<@")) {
                String victimID = callArgs[1].substring(2, callArgs[1].length() - 1);
                User victimUser = state.getServer().getMemberById(victimID).get();
                String victimName = victimUser.getName();
                User voterUser = event.getMessage().getAuthor().asUser().get();

                Ballot existingVotersBallot = findVoterBallot(voterUser);
                if (existingVotersBallot != null) {
                    existingVotersBallot.voters.remove(voterUser);
                    existingVotersBallot.votes--;
                    if (existingVotersBallot.votes == 0) {
                        victimVotes.remove(existingVotersBallot);
                    }
                }

                Ballot victimBallot = findVictimBallot(victimUser);
                if (victimBallot == null) {
                    victimBallot = new Ballot(victimUser);
                    victimVotes.add(victimBallot);
                } else {
                    victimBallot.votes++;
                }
                victimBallot.voters.add(voterUser);

                EmbedBuilder voteEmbed = new EmbedBuilder()
                        .setTitle("Current Votes")
                        .setColor(Color.RED)
                        .setFooter("TMBST");
                for (Ballot b : victimVotes) {
                    voteEmbed.addField(b.candidate.getName(), Integer.toString(b.votes));
                }
                event.getChannel().sendMessage(voteEmbed);

            } else {
                event.getChannel().sendMessage(usage);
            }
        }
    }

    private void startKillTimer() {
        ScheduledExecutorService timerService = Executors.newScheduledThreadPool(1);
        ScheduledFuture killTimer = timerService.schedule(new Runnable() {
            @Override
            public void run() {
                Ballot max = null;
                for (Ballot b : victimVotes) {
                    if (max == null || (b.votes > max.votes)) {
                        max = b;
                    }
                }
                EmbedBuilder resultsEmbed = new EmbedBuilder();
                resultsEmbed.setFooter("TMBST");
                if (max == null) {
                    resultsEmbed.setTitle("We have decided to lay the banhammer upon... no one!?!?");
                    state.setDeadByMafia(null);
                } else {
                    resultsEmbed.setTitle("We have decided to lay the banhammer upon " + max.candidate.getName());
                    resultsEmbed.setThumbnail(max.candidate.getAvatar());
                    state.setDeadByMafia(max.candidate);
                }
                mafiaChannel.sendMessage(resultsEmbed).join();
                endListening();
                session.startDay();
            }
        }, VOTETIME, TimeUnit.SECONDS);
    }

    private void endListening() {
        Main.api.removeListener(this);
    }

/*
    I'm not sure if two User objects are equal if and only if they represent the same user (may have two User objects
    that are unequal, but may represent the same user), so I'm being explicit and comparing their IDs to be sure.
    I guess using list.remove(Object o) with that User isn't explicit like that... oh well.
*/

    //checks if the user voted. returns the ballot they voted on if they did, null if not
    private Ballot findVoterBallot(User voter) {
        for (Ballot b : victimVotes) {
            for (User u : b.voters) {
                if (u.getId() == voter.getId()) {
                    return b;
                }
            }
        }
        return null;
    }

    //returns null if not found
    private Ballot findVictimBallot(User victim) {
        for (Ballot b : victimVotes) {
            if (victim.getId() == b.candidate.getId()) {
                return b;
            }
        }
        return null;
    }

}

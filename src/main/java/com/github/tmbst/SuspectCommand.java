package com.github.tmbst;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;

public class SuspectCommand implements MessageCreateListener {

    private static final String commandRegex = "!suspect(\\s+.*)?";
    private static final String usage = "Usage: !suspect <@user>";
    private final ServerTextChannel townChannel;
    private static final String hammerEmoji = "\uD83D\uDD28";
    private static final String innocentEmoji = "\uD83D\uDE07";

    public SuspectCommand(ServerTextChannel townChannel) {
        super();
        this.townChannel = townChannel;
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
                Main.api.removeListener(this);
                EmbedBuilder voteEmbed = new EmbedBuilder()
                        .setTitle("Vote now! Should we give " + accusedName + " the banhammer?")
                        .setDescription("React to vote! 50% vote required.")
                        .setAuthor(accuser.getName())
                        .setColor(Color.BLUE)
                        .setFooter("TMBST");
                Message voteMessage = event.getChannel().sendMessage(voteEmbed).join();
                voteMessage.addReaction(hammerEmoji);
                voteMessage.addReaction(innocentEmoji);
                voteMessage.addReactionAddListener(emojiAddEvent -> {
                    if (emojiAddEvent.getEmoji().equalsEmoji(hammerEmoji)) {

                    } else if (emojiAddEvent.getEmoji().equalsEmoji(innocentEmoji)) {

                    }
                });
            } else {
                event.getChannel().sendMessage(usage);
            }
        }
    }

}

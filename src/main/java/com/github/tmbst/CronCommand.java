package com.github.tmbst;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.concurrent.*;

public class CronCommand implements MessageCreateListener {
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessage().getContent().equals("!start")) {
            ScheduledExecutorService cron = Executors.newScheduledThreadPool(1);
            ScheduledFuture schedule = cron.schedule(new Runnable() {
                 @Override
                 public void run() {
                     event.getChannel().sendMessage("I fucking hate Isabella from phinnis n ferd. That little bitch is so nosy and she always barges on in askin “Hi phinnis, watchyaaa doooooin?” Like bitch leav phinnis alone he is building shit \uD83D\uDE3C fo the block to hop over\uD83D\uDE24. Just because you want that Phinnis dick\uD83C\uDF46 don’t mean that you can invade they backyard\uD83D\uDE10\uD83D\uDE10.");
                 }
             },
            8,
            TimeUnit.MINUTES);
        }
    }
}

package com.github.tmbst;

import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.List;

public class TearDownCommand implements MessageCreateListener {

    private Session session;

    public TearDownCommand(Session session) {
        this.session = session;
    }


    @Override
    public void onMessageCreate(MessageCreateEvent event) {

        if (event.getMessage().getContent().equalsIgnoreCase("!teardown")) {

            // Delete channels
            session.state.getTownChannel().delete();
            session.state.getGraveyardChannel().delete();
            session.state.getMafiaChannel().delete();


            // Delete roles
            List<User> users = session.state.getUsersList();
            for (User u: users) {
                u.removeRole(session.state.getDeadRole()).join();
                u.removeRole(session.state.getAliveRole()).join();
            }

        }
    }
}

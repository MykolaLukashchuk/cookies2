package server.actors;

import akka.actor.AbstractActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserActor extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserActor.class);


    @Override
    public Receive createReceive() {
        return null;
    }
}

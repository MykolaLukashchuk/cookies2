package server.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.CustomException;
import server.Server;
import server.actors.UserActor;
import server.model.User;
import server.model.request.UserRequest;
import server.model.responce.UserResponse;
import server.repo.ConfigRepo;
import server.repo.UserRepo;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ActorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActorHandler.class);
    private final ActorSystem akkaSystem;
    private final UserRepo userRepo;
    private final Map<String, ActorRef> actors;
    private final ConfigRepo configRepo;

    @Inject
    public ActorHandler(UserRepo userRepo, ConfigRepo configRepo) {
        akkaSystem = Server.akkaSystem;
        this.userRepo = userRepo;
        actors = new HashMap<>();
        this.configRepo = configRepo;
    }

    public void createActor(User user) {
        ActorRef actorRef = akkaSystem.actorOf(Props.create(UserActor.class, user, userRepo, configRepo), user.getIdAsString());
        actors.put(user.getIdAsString(), actorRef);
    }

    public UserResponse auth(UserRequest request) throws CustomException {
        final UserResponse response = new UserResponse();
        User user = userRepo.findUserBySeed(request.getSeed());
        if (user == null) {
            userRepo.insertNewUser(request.getSeed());
            response.setNewDevice(true);
            return response;
        } else if (user.getNickname() == null && request.getNickname() == null) {
            response.setNewDevice(true);
            throw new CustomException("New user's nickname cannot be null.");
        } else if (user.getNickname() == null && request.getNickname() != null) {
            user.setNickname(request.getNickname());
            user.setUpdated(new Date());
            userRepo.updateUser(user);
        }
        response.setToken(user.getIdAsString());
        response.setNickname(user.getNickname());

        if (actors.get(user.getIdAsString()) == null) {
            createActor(user);
        }
        return response;
    }

    public ActorRef getActor(String token) {
        return actors.get(token);
    }
}

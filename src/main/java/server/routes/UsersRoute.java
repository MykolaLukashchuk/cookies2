package server.routes;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.CustomException;
import server.config.ActorHandler;
import server.model.request.Request;
import server.model.request.UserRequest;
import server.model.responce.Response;
import server.model.responce.UserResponse;
import server.repo.UserRepo;
import server.utils.CustomRuntimeException;
import server.utils.EncryptUtils;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import static akka.http.javadsl.server.RequestVals.entityAs;
import static server.Server.mapper;
import static server.utils.EncryptUtils.decryptUser;

public class UsersRoute extends AllDirectives {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersRoute.class);
    private final UserRepo userRepo;
    private final ActorHandler actorHandler;

    @Inject
    public UsersRoute(UserRepo userRepo, ActorHandler actorHandler) {
        this.userRepo = userRepo;
        this.actorHandler = actorHandler;
    }

    public Route getRoute() {
        return pathPrefix("users").route(
                post(pathEndOrSingleSlash().route(
                        handleWith(entityAs(jsonAs(MasterRequest.class)),
                                (ctx, request) -> {
                                    try {
                                        Optional<String> requestString = Optional.of(EncryptUtils.decryptAsMaster(request.getBody()));
                                        List users = requestString.filter(req -> req.equals("master"))
                                                .map(s -> {
                                                    LOGGER.info("Request to \"/users\"");
                                                    if (request.getSeed() != null) {
                                                        return Arrays.asList(userRepo.findUserBySeed(request.getSeed()));
                                                    } else if (request.getUserId() != null) {
                                                        return Arrays.asList(userRepo.find(request.getUserId()));
                                                    }
                                                    return userRepo.getAll();
                                                }).orElseThrow(() -> new CustomException("Wrong request."));
                                        return ctx.completeAs(Jackson.json(), users);
                                    } catch (CustomRuntimeException e) {
                                        LOGGER.error(e.getMessage());
                                        return ctx.completeAs(Jackson.json(), new Response(null, e.getMessage()));
                                    } catch (Exception e) {
                                        LOGGER.error(e.getMessage(), e);
                                        return ctx.completeAs(Jackson.json(), new Response(null, e.getMessage()));
                                    }
                                })
                )),
                pathSuffix("auth").route(
                        post(pathEndOrSingleSlash()
                                .route(handleWith(entityAs(jsonAs(Request.class)),
                                        (ctx, request) -> {
                                            try {
                                                Optional<UserRequest> userRequest = decryptUser(request.getBody(), UserRequest.class);

                                                Response response = userRequest.map(req -> {
                                                    LOGGER.info("Request to \"/users/auth\" Body: " + req.toString());
                                                    try {
//                                                        return userRepo.auth(userRequest.get());
                                                        return actorHandler.auth(userRequest.get());
                                                    } catch (CustomException e) {
                                                        return new UserResponse(e.getMessage());
                                                    }
                                                }).map(resp -> {
                                                    LOGGER.debug("Response. Body: " + resp.toString());
                                                    return resp;
                                                }).map(resp -> {
                                                    try {
                                                        if (resp.getMessage() == null) {
                                                            return new Response(EncryptUtils.encryptAsUser(mapper.writeValueAsString(resp)), null);
                                                        } else {
                                                            return new Response(null, resp.getMessage());
                                                        }
                                                    } catch (JsonProcessingException e) {
                                                        LOGGER.error(e.getMessage(), e);
                                                        return null;
                                                    }
                                                }).orElseThrow(() -> new CustomRuntimeException("Trouble"));
                                                return ctx.completeAs(Jackson.json(), response);

                                            } catch (CustomRuntimeException e) {
                                                LOGGER.error(e.getMessage());
                                                return ctx.completeAs(Jackson.json(), new Response(null, e.getMessage()));
                                            } catch (Exception e) {
                                                LOGGER.error(e.getMessage(), e);
                                                return ctx.completeAs(Jackson.json(), new Response(null, e.getMessage()));
                                            }
                                        }
                                        )
                                )
                        )
                ),
                pathSuffix("do").route(
                        get(pathEndOrSingleSlash()
                                .route(
                                        get(pathEndOrSingleSlash().route(
                                                handleWith(ctx -> {
                                                    if (userRepo.doSmth()) {
                                                        return ctx.completeWithStatus(200);
                                                    } else {
                                                        return ctx.completeWithStatus(500);
                                                    }
                                                })
                                                )
                                        )
                                )
                        )
                )
        );
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MasterRequest {
        private String body;
        private String userId;
        private String seed;
    }
}

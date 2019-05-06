package server.routes;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.CustomException;
import server.model.request.UserRequest;
import server.model.responce.UserResponse;
import server.repo.UserRepo;
import server.utils.EncryptUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import static akka.http.javadsl.server.RequestVals.entityAs;

public class UsersRoute extends AllDirectives {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersRoute.class);
    private final UserRepo userRepo;
    private final static ObjectMapper mapper = new ObjectMapper();

    @Inject
    public UsersRoute(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public Route getRoute() {
        return pathPrefix("users").route(
                post(pathEndOrSingleSlash().route(
                        handleWith(entityAs(jsonAs(Request.class)),
                                (ctx, request) -> {
                                    try {
                                        Optional<String> requestString = Optional.of(EncryptUtils.decryptAsMaster(request.getBody()));
                                        List users = requestString.filter(req -> req.equals("master"))
                                                .map(s -> {
                                                    LOGGER.info("Request to \"/users\"");
                                                    return userRepo.getAll();
                                                }).orElseThrow(() -> new CustomException("Wrong request."));
                                        return ctx.completeAs(Jackson.json(), users);
                                    } catch (Exception e) {
                                        return ctx.completeAs(Jackson.json(), new Response(null, e.getMessage()));
                                    }
                                })
                )),
                pathSuffix("auth").route(
                        post(pathEndOrSingleSlash()
                                .route(handleWith(entityAs(jsonAs(Request.class)),
                                        (ctx, request) -> {
                                            try {
                                                Optional<UserRequest> userRequest = decryptUser(request, UserRequest.class);

                                                Response response = userRequest.map(req -> {
                                                    LOGGER.info("Request to \"/users/auth\" Body: " + req.toString());
                                                    try {
                                                        return userRepo.auth(userRequest.get());
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
                                                }).orElseThrow(() -> new CustomException("Trouble"));
                                                return ctx.completeAs(Jackson.json(), response);

                                            } catch (CustomException e) {
                                                LOGGER.info(e.getMessage());
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

    private <T> Optional<T> decryptUser(Request request, Class<T> t) throws CustomException {
        try {
            return Optional.of(mapper.readValue(EncryptUtils.decryptAsUser(request.getBody()), t));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new CustomException(e.getMessage());
        }
    }

    private <T> Optional<T> decryptMaster(Request request, Class<T> t) throws CustomException {
        try {
            return Optional.of(mapper.readValue(EncryptUtils.decryptAsMaster(request.getBody()), t));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new CustomException(e.getMessage());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String body;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Response {
        private String body;
        private String message;
    }
}

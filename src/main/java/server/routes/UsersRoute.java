package server.routes;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.CustomException;
import server.model.User;
import server.model.request.UserRequest;
import server.model.responce.UserResponse;
import server.repo.UserRepo;

import javax.inject.Inject;

import java.util.List;

import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import static akka.http.javadsl.server.RequestVals.entityAs;

public class UsersRoute extends AllDirectives {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsersRoute.class);
    private final UserRepo userRepo;

    @Inject
    public UsersRoute(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public Route getRoute() {
        return pathPrefix("users").route(
                get(pathEndOrSingleSlash().route(
                        handleWith(requestContext -> {
                            LOGGER.info("Request to \"/users\"");
                            List<User> allUsers = userRepo.getAll();
                            return requestContext.completeAs(Jackson.json(), allUsers);
                        })
                )),
                pathSuffix("auth").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(UserRequest.class)),
                                                (ctx, request) -> {
                                                    LOGGER.info("Request to \"/users/auth\" Body: " + request.toString());
                                                    UserResponse response;
                                                    try {
                                                        response = userRepo.auth(request);
                                                    } catch (CustomException e) {
                                                        response = new UserResponse(e.getMessage());
                                                    }
                                                    LOGGER.debug("Response. Body: " + response.toString());
                                                    return ctx.completeAs(Jackson.json(), response);
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
}

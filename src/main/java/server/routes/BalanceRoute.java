package server.routes;

import akka.actor.ActorRef;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import server.CustomException;
import server.config.ActorHandler;
import server.core.CheckedFunction;
import server.model.request.BalanceAdjustRequest;
import server.model.request.BalanceRequest;
import server.model.request.BoardRequest;
import server.model.request.Request;
import server.model.responce.Response;
import server.repo.UserRepo;
import server.utils.CustomRuntimeException;
import server.utils.EncryptUtils;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import static akka.http.javadsl.server.RequestVals.entityAs;
import static server.Server.mapper;
import static server.utils.EncryptUtils.decryptUser;

public class BalanceRoute extends AllDirectives {
    private static final int TIMEOUT = 2000;
    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceRoute.class);
    private final ActorHandler actorHandler;
    private final UserRepo userRepo;

    @Inject
    public BalanceRoute(ActorHandler actorHandler, UserRepo userRepo) {
        this.actorHandler = actorHandler;
        this.userRepo = userRepo;
    }

    public Route getRoute() {
        return pathPrefix("balance").route(
                pathSuffix("get").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(Request.class)),
                                                (ctx, request) -> {
                                                    try {
                                                        Optional<BalanceRequest> balanceRequest = decryptUser(request, BalanceRequest.class);

                                                        Response response = balanceRequest.stream()
                                                                .peek(req -> LOGGER.info("Request to \"/balance/get\" Body: " + req.toString()))
                                                                .map(wrapper(req -> {
                                                                    ActorRef actor = actorHandler.getActor(req.getToken());
                                                                    if (actor == null) {
                                                                        throw new Exception("User not authorize.");
                                                                    }
                                                                    Future<Object> objectFuture = Patterns.ask(actor, req, TIMEOUT);
                                                                    return Await.result(objectFuture, Duration.apply(TIMEOUT, TimeUnit.MILLISECONDS));
                                                                }))
                                                                .peek(resp -> LOGGER.debug("Response. Body: " + resp.toString()))
                                                                .map(resp -> {
                                                                    try {
                                                                        return new Response(EncryptUtils.encryptAsUser(mapper.writeValueAsString(resp)), null);
                                                                    } catch (JsonProcessingException e) {
                                                                        LOGGER.error(e.getMessage(), e);
                                                                        return new Response(null, e.getMessage());
                                                                    }
                                                                }).findFirst()
                                                                .orElseThrow(() -> new CustomException("Trouble"));

                                                        return ctx.completeAs(Jackson.json(), response);
                                                    } catch (CustomRuntimeException e) {
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
                pathSuffix("adjust").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(Request.class)),
                                                (ctx, request) -> {
                                                    try {
                                                        Optional<BalanceAdjustRequest> balanceAdjustRequest = decryptUser(request, BalanceAdjustRequest.class);

                                                        Response response = balanceAdjustRequest.stream()
                                                                .peek(req -> LOGGER.info("Request to \"/balance/adjust\" Body: " + req.toString()))
                                                                .map(wrapper(req -> {
                                                                    ActorRef actor = actorHandler.getActor(req.getToken());
                                                                    if (actor == null) {
                                                                        throw new Exception("User not authorize.");
                                                                    }
                                                                    Future<Object> objectFuture = Patterns.ask(actor, req, TIMEOUT);
                                                                    return Await.result(objectFuture, Duration.apply(TIMEOUT, TimeUnit.MILLISECONDS));
                                                                }))
                                                                .peek(resp -> LOGGER.debug("Response. Body: " + resp.toString()))
                                                                .map(resp -> {
                                                                    try {
                                                                        return new Response(EncryptUtils.encryptAsUser(mapper.writeValueAsString(resp)), null);
                                                                    } catch (JsonProcessingException e) {
                                                                        LOGGER.error(e.getMessage(), e);
                                                                        return new Response(null, e.getMessage());
                                                                    }
                                                                }).findFirst()
                                                                .orElseThrow(() -> new CustomException("Trouble"));

                                                        return ctx.completeAs(Jackson.json(), response);
                                                    } catch (CustomRuntimeException e) {
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
                pathSuffix("board").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(Request.class)),
                                                (ctx, request) -> {
                                                    try {
                                                        Optional<BoardRequest> boardRequest = decryptUser(request, BoardRequest.class);

                                                        Response response = boardRequest.stream()
                                                                .peek(req -> LOGGER.info("Request to \"/balance/board\" Body: " + req.toString()))
                                                                .map(wrapper(req -> {
                                                                    ActorRef actor = actorHandler.getActor(req.getToken());
                                                                    if (actor == null) {
                                                                        throw new Exception("User not authorize.");
                                                                    }
                                                                    return userRepo.getLiederBoard(req);
                                                                }))
                                                                .peek(resp -> LOGGER.debug("Response. Body: " + resp.toString()))
                                                                .map(resp -> {
                                                                    try {
                                                                        return new Response(EncryptUtils.encryptAsUser(mapper.writeValueAsString(resp)), null);
                                                                    } catch (JsonProcessingException e) {
                                                                        LOGGER.error(e.getMessage(), e);
                                                                        return new Response(null, e.getMessage());
                                                                    }
                                                                }).findFirst()
                                                                .orElseThrow(() -> new CustomException("Trouble"));

                                                        return ctx.completeAs(Jackson.json(), response);
                                                    } catch (CustomRuntimeException e) {
                                                        return ctx.completeAs(Jackson.json(), new Response(null, e.getMessage()));
                                                    } catch (Exception e) {
                                                        LOGGER.error(e.getMessage(), e);
                                                        return ctx.completeAs(Jackson.json(), new Response(null, e.getMessage()));
                                                    }
                                                }
                                        )
                                )
                        )
                )
        );
    }

    private <T, R, E extends Exception> Function<T, R> wrapper(CheckedFunction<T, R, E> fe) {
        return arg -> {
            try {
                return fe.apply(arg);
            } catch (Exception e) {
                throw new CustomRuntimeException(e.getMessage());
            }
        };
    }
}

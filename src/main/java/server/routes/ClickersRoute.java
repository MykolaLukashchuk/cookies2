package server.routes;

import akka.actor.ActorRef;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.RequestContext;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.RouteResult;
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
import server.model.request.ClickersBalanceAdjustRequest;
import server.model.request.ClickersBalanceRequest;
import server.model.request.CollectRequest;
import server.model.request.Request;
import server.model.responce.Response;
import server.utils.CustomRuntimeException;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import static akka.http.javadsl.server.RequestVals.entityAs;
import static server.Server.mapper;
import static server.utils.EncryptUtils.decryptUser;
import static server.utils.EncryptUtils.encryptAsUser;

public class ClickersRoute extends AllDirectives {
    public static final int TIMEOUT = 2000;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClickersRoute.class);
    private final ActorHandler actorHandler;

    @Inject
    public ClickersRoute(ActorHandler actorHandler) {
        this.actorHandler = actorHandler;
    }

    public Route getRoute() {
        return pathPrefix("clickers").route(
                pathSuffix("balance").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(Request.class)),
                                                (ctx, request) -> {
                                                    try {
                                                        Optional<ClickersBalanceRequest> clickerRequest = decryptUser(request, ClickersBalanceRequest.class);

                                                        Response response = clickerRequest.stream()
                                                                .peek(req -> LOGGER.info("Request to \"/clickers/balance\" Body: " + req.toString()))
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
                                                                        return new Response(encryptAsUser(mapper.writeValueAsString(resp)), null);
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
                                                        Response response = decryptUser(request, ClickersBalanceAdjustRequest.class).stream()
                                                                .peek(req -> LOGGER.info("Request to \"/clickers/adjust\" Body: " + req.toString()))
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
                                                                        return new Response(encryptAsUser(mapper.writeValueAsString(resp)), null);
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
                pathPrefix("collect").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(
                                                entityAs(jsonAs(Request.class)),
                                                (ctx, request) -> {
                                                    try {
                                                        Response response = decryptUser(request, CollectRequest.class).stream()
                                                                .peek(req -> LOGGER.info("Request to \"/clickers/collect\" Body: " + req.toString()))
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
                                                                        return new Response(encryptAsUser(mapper.writeValueAsString(resp)), null);
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
                                ))
                )
        );
    }

    private RouteResult getRouteError(RequestContext ctx, Exception e) {
        return ctx.completeAs(Jackson.json(), new Response(null, e.getMessage()));
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

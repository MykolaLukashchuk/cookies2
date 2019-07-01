package server.routes;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.CustomException;
import server.core.CheckedFunction;
import server.model.request.AdjustClickersBalanceRequest;
import server.model.request.ClickersBalanceRequest;
import server.model.request.Request;
import server.model.responce.Response;
import server.repo.ClickersRepo;
import server.utils.CustomRuntimeException;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Function;

import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import static akka.http.javadsl.server.RequestVals.entityAs;
import static server.Server.mapper;
import static server.utils.EncryptUtils.decryptUser;
import static server.utils.EncryptUtils.encryptAsUser;

public class ClickersRoute extends AllDirectives {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClickersRoute.class);
    private final ClickersRepo clickersRepo;

    @Inject
    public ClickersRoute(ClickersRepo clickersRepo) {
        this.clickersRepo = clickersRepo;
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
                                                                        .map(wrapper(clickersRepo::getBalance))
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
                                                                Optional<AdjustClickersBalanceRequest> clickerRequest = decryptUser(request, AdjustClickersBalanceRequest.class);

                                                                Response response = clickerRequest.stream()
                                                                        .peek(req -> LOGGER.info("Request to \"/clickers/adjust\" Body: " + req.toString()))
                                                                        .map(wrapper(clickersRepo::adjustBalance))
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
                pathSuffix("price").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(Request.class)),
                                                (ctx, request) -> {
                                                    try {
                                                        Optional<ClickersBalanceRequest> clickerRequest = decryptUser(request, ClickersBalanceRequest.class);

                                                        Response response = clickerRequest.stream()
                                                                .peek(req -> LOGGER.info("Request to \"/clickers/price\" Body: " + req.toString()))
                                                                .map(wrapper(clickersRepo::getPrice))
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
                )
        );
    }

    private <T, R, E extends Exception>  Function<T, R> wrapper(CheckedFunction<T, R, E> fe) {
        return arg -> {
            try {
                return fe.apply(arg);
            } catch (Exception e) {
                throw new CustomRuntimeException(e.getMessage());
            }
        };
    }
}

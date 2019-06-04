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
import server.model.request.BalanceAdjustRequest;
import server.model.request.BalanceRequest;
import server.model.request.BoardRequest;
import server.model.responce.BoardResponse;
import server.repo.BalanceRepo;
import server.utils.EncryptUtils;

import javax.inject.Inject;
import java.util.Optional;

import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import static akka.http.javadsl.server.RequestVals.entityAs;

public class BalanceRoute extends AllDirectives {
    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceRoute.class);
    private final BalanceRepo balanceRepo;
    private final static ObjectMapper mapper = new ObjectMapper();


    @Inject
    public BalanceRoute(BalanceRepo balanceRepo) {
        this.balanceRepo = balanceRepo;
    }

    public Route getRoute() {
        return pathPrefix("balance").route(
                get(pathEndOrSingleSlash().route(
                        handleWith(requestContext -> {
                            // TODO: 03.05.2019 master
                            LOGGER.info("Request to \"/balance\"");
                            return requestContext.completeAs(Jackson.json(), balanceRepo.getAll());
                        })
                )),
                pathSuffix("get").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(Request.class)),
                                                (ctx, request) -> {
                                                    try {
                                                        Optional<BalanceRequest> balanceRequest = decryptUser(request, BalanceRequest.class);

                                                        Response response = balanceRequest.stream()
                                                                .peek(req -> LOGGER.info("Request to \"/balance/get\" Body: " + req.toString()))
                                                                .map(balanceRepo::get)
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
                                                    } catch (CustomException e) {
                                                        LOGGER.info(e.getMessage());
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
                                                                .map(balanceRepo::adjust)
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
                                                    } catch (CustomException e) {
                                                        LOGGER.info(e.getMessage());
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
                                                                .map(balanceRepo::getLiederBoard)
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
                                                    } catch (CustomException e) {
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
                                                    if (balanceRepo.doSmth()) {
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

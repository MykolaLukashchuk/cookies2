package server.routes;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.model.request.BalanceAdjustRequest;
import server.model.request.BalanceRequest;
import server.model.request.BoardRequest;
import server.model.responce.BalanceResponse;
import server.model.responce.BoardResponse;
import server.repo.BalanceRepo;

import javax.inject.Inject;

import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import static akka.http.javadsl.server.RequestVals.entityAs;

public class BalanceRoute extends AllDirectives {
    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceRoute.class);
    private final BalanceRepo balanceRepo;

    @Inject
    public BalanceRoute(BalanceRepo balanceRepo) {
        this.balanceRepo = balanceRepo;
    }

    public Route getRoute() {
        return pathPrefix("balance").route(
                get(pathEndOrSingleSlash().route(
                        handleWith(requestContext -> {
                            LOGGER.info("Request to \"/balance\"");
                            return requestContext.completeAs(Jackson.json(), balanceRepo.getAll());
                        })
                )),
                pathSuffix("get").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(BalanceRequest.class)),
                                                (ctx, request) -> {
                                                    LOGGER.info("Request to \"/balance/get\" Body: " + request.toString());
                                                    BalanceResponse response = balanceRepo.get(request);
                                                    LOGGER.debug("Response. Body: " + response.toString());
                                                    return ctx.completeAs(Jackson.json(), response);
                                                }
                                        )
                                )
                        )
                ),
                pathSuffix("adjust").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(BalanceAdjustRequest.class)),
                                                (ctx, request) -> {
                                                    LOGGER.info("Request to \"/balance/adjust\" Body: " + request.toString());
                                                    BalanceResponse response = balanceRepo.adjust(request);
                                                    LOGGER.debug("Response. Body: " + response.toString());
                                                    return ctx.completeAs(Jackson.json(), response);
                                                }
                                        )
                                )
                        )
                ),
                pathSuffix("board").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(BoardRequest.class)),
                                                (ctx, request) -> {
                                                    LOGGER.info("Request to \"/balance/board\" Body: " + request.toString());
                                                    BoardResponse response = balanceRepo.getLiederBoard(request);
                                                    LOGGER.debug("Response. Body: " + response.toString());
                                                    return ctx.completeAs(Jackson.json(), response);
                                                }
                                        )
                                )
                        )
                )
        );
    }
}

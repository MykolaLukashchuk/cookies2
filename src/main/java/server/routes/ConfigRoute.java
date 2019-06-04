package server.routes;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.CustomException;
import server.Server;
import server.model.ConfigItem;
import server.model.request.BoardRequest;
import server.model.request.ConfigRequest;
import server.model.request.Request;
import server.model.request.UserRequest;
import server.model.responce.Response;
import server.model.responce.UserResponse;
import server.repo.ConfigRepo;
import server.repo.UserRepo;
import server.utils.EncryptUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import static akka.http.javadsl.server.RequestVals.entityAs;
import static server.Server.*;

public class ConfigRoute extends AllDirectives {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRoute.class);
    private final ConfigRepo configRepo;

    @Inject
    public ConfigRoute(ConfigRepo configRepo) {
        this.configRepo = configRepo;
    }

    public Route getRoute() {
        return pathPrefix("config").route(
                pathSuffix("get").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(Request.class)),
                                                (ctx, request) -> {
                                                    try {
                                                        Optional<ConfigRequest> boardRequest = decryptUser(request, ConfigRequest.class);

                                                        Response response = boardRequest.stream()
                                                                .peek(req -> LOGGER.info("Request to \"/config/get\" Body: " + req.toString()))
                                                                .map(req -> configRepo.getConfig(req))
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
                pathSuffix("put").route(
                        put(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(Request.class)),
                                                (ctx, request) -> {
                                                    try {
                                                        Optional<ConfigItem> item = decryptMaster(request, ConfigItem.class);

                                                        Boolean b = item.stream()
                                                                .peek(req -> LOGGER.info("Request to \"/config/put\" Body: " + req.toString()))
                                                                .map(req -> configRepo.putConfig(req))
                                                                .peek(resp -> LOGGER.debug("Response. Body: " + resp.toString()))
                                                                .findFirst()
                                                                .isPresent();

                                                        return ctx.completeWithStatus(b ? 200 : 500);
                                                    } catch (CustomException e) {
                                                        LOGGER.error(e.getMessage(), e);
                                                        return ctx.completeAs(Jackson.json(), new Response(null, e.getMessage()));
                                                    }
                                                }
                                        )
                                )
                        )
                ),
                pathSuffix("del").route(
                        post(pathEndOrSingleSlash()
                                .route(
                                        handleWith(entityAs(jsonAs(Request.class)),
                                                (ctx, request) -> {
                                                    try {
                                                        Optional<ConfigItem> item =  decryptMaster(request, ConfigItem.class);

                                                        Boolean b = item.stream()
                                                                .peek(req -> LOGGER.info("Request to \"/config/del\" Body: " + req))
                                                                .map(configRepo::deleteConfig)
                                                                .peek(resp -> LOGGER.debug("Response. Body: " + resp.toString()))
                                                                .findFirst()
                                                                .isPresent();

                                                        return ctx.completeWithStatus(b ? 200 : 500);
                                                    } catch (CustomException e) {
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

    private Optional<String> decryptMasterAsString(Request request) throws CustomException {
        try {
            return Optional.of(EncryptUtils.decryptAsMaster(request.getBody()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new CustomException(e.getMessage());
        }
    }
}

package server.actors;

import akka.actor.AbstractActor;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.CustomException;
import server.Server;
import server.core.CheckedFunction;
import server.enums.ConfigKeys;
import server.model.ConfigItem;
import server.model.User;
import server.model.request.BalanceAdjustRequest;
import server.model.request.BalanceRequest;
import server.model.request.ClickersBalanceAdjustRequest;
import server.model.request.ClickersBalanceRequest;
import server.model.responce.BalanceResponse;
import server.model.responce.ClickersBalanceResponse;
import server.repo.ConfigRepo;
import server.repo.UserRepo;
import server.service.LogService;
import server.utils.CustomRuntimeException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class UserActor extends AbstractActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserActor.class);
    private final UserRepo userRepo;
    private final ConfigRepo configRepo;
    private User user;

    public UserActor(User user, UserRepo userRepo, ConfigRepo configRepo) {
        this.user = user;
        this.userRepo = userRepo;
        this.configRepo = configRepo;
        LOGGER.debug(String.format("Created Actor. Name: %s", user.getIdAsString()));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BalanceRequest.class, this::getBalance)
                .match(BalanceAdjustRequest.class, this::balanceAdjust)
                .match(ClickersBalanceRequest.class, this::getClickerBalance)
                .match(ClickersBalanceAdjustRequest.class, (request) -> {
                    try {
                        Map<String, Long> clickerBalanceMap = user.getClickerBalance();
                        if (clickerBalanceMap == null) {
                            clickerBalanceMap = new HashMap<>();
                        }
                        Long clickerBalance = clickerBalanceMap.get(request.getClickerId());
                        if (clickerBalance == null) {
                            clickerBalance = 0L;
                        }
                        Long finalL = Long.valueOf(++clickerBalance);
                        Long priceInCookies = Optional.of(configRepo.getConfigValueByKey(ConfigKeys.CLICKERS.getKey()))
                                .map(wrapper(s -> Server.mapper.readValue(s, new TypeReference<List<ConfigItem.Clicker>>() {
                                })))
                                .map(o -> (List<ConfigItem.Clicker>) o)
                                .map(wrapper(clickers ->
                                        clickers
                                                .stream()
                                                .filter(clicker -> clicker.getId().equals(request.getClickerId()))
                                                .findFirst()
                                                .orElseThrow(() -> new CustomException("Wrong clickerId")))
                                )
                                .map(ConfigItem.Clicker::getPrice)
                                .map(startPrice -> (long) (startPrice * Math.pow(2.0, finalL)))
                                .get();
                        long newCookiesBalance = user.getCookiesBalance() - priceInCookies;
                        if (newCookiesBalance < 0) {
                            throw new CustomRuntimeException("Not enough clicker balance.");
                        }
                        clickerBalanceMap.put(request.getClickerId(), clickerBalance);
                        user.setCookiesBalance(newCookiesBalance);
                        user.setClickerBalance(clickerBalanceMap);
                        user.setUpdated(new Date());
                        userRepo.updateUser(user);
                        LOGGER.debug(String.format("New clickerBalance (id: %s, balance: %d) for userId = %s", request.getClickerId(), clickerBalance, user.getIdAsString()));
                        getSender().tell(new ClickersBalanceResponse(user.getIdAsString(), user.getClickerBalance()), getSelf());
                        CompletableFuture.runAsync(() -> LogService.getInstance().clickerBalanceLog(user.getIdAsString(), user.getClickerBalance(), new Date()), Server.LOG_POOL);
                    } catch (CustomRuntimeException e) {
                        getSender().tell(new ClickersBalanceResponse(user.getIdAsString(), e.getMessage()), getSelf());
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                        getSender().tell(new ClickersBalanceResponse(user.getIdAsString(), e.getMessage()), getSelf());
                    }
                })
                .matchAny(o -> LOGGER.error("Received unknown message."))
                .build();
    }

    private void getClickerBalance(ClickersBalanceRequest request) {
        try {
            Map<String, Long> balance = user.getClickerBalance();
            if (balance == null) {
                user.setClickerBalance(new HashMap<>());
            }
            getSender().tell(new ClickersBalanceResponse(user.getIdAsString(), user.getClickerBalance()), getSelf());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            getSender().tell(new ClickersBalanceResponse(user.getIdAsString(), e.getMessage()), getSelf());
        }
    }

    private void balanceAdjust(BalanceAdjustRequest request) {
        try {
            long newBalance = user.getCookiesBalance() + request.getActivity();
            if (newBalance < 0) {
                throw new CustomException("Not enough balance");
            }
            user.setCookiesBalance(newBalance);
            user.setUpdated(new Date());
            userRepo.updateUser(user);
            LOGGER.debug(String.format("New cookiesBalance = %s for userId = %s", user.getCookiesBalance(), user.getIdAsString()));
            CompletableFuture.runAsync(() -> LogService.getInstance().cookiesBalanceLog(user.getIdAsString(), request.getActivity(), user.getCookiesBalance(), new Date()), Server.LOG_POOL);
            getSender().tell(new BalanceResponse(user.getIdAsString(), user.getCookiesBalance()), getSelf());
        } catch (CustomException e) {
            getSender().tell(new BalanceResponse(user.getIdAsString(), e.getMessage()), getSelf());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            getSender().tell(new BalanceResponse(user.getIdAsString(), e.getMessage()), getSelf());
        }
    }

    private void getBalance(BalanceRequest request) {
        try {
            if (user.getCookiesBalance() == null) {
                user.setCookiesBalance(0L);
                userRepo.updateUser(user);
            }
            getSender().tell(new BalanceResponse(user.getIdAsString(), user.getCookiesBalance()), getSelf());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            getSender().tell(new BalanceResponse(user.getIdAsString(), e.getMessage()), getSelf());
        }
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
package server.repo;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.config.MongoClientManager;
import server.model.Adjustment;
import server.model.Balance;
import server.model.User;
import server.model.request.BalanceAdjustRequest;
import server.model.request.BalanceRequest;
import server.model.request.BoardRequest;
import server.model.responce.BalanceResponse;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import server.model.responce.BoardResponse;

public class BalanceRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceRepo.class);

    private final UserRepo userRepo;
    private MongoCollection<Balance> collection;
    private MongoCollection<Adjustment> adjustments;

    @Inject
    public BalanceRepo(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public List finAll() {
        List<Balance> list = new ArrayList<>();
        FindIterable<Balance> iterable = getCollection().find();
        for (Balance doc : iterable) {
            list.add(doc);
        }
        return list;
    }

    private MongoCollection<Balance> getCollection() {
        if (collection == null) {
            collection = MongoClientManager.getCollection("balance", Balance.class);
        }
        return collection;
    }

    private MongoCollection<Adjustment> getAdjustmentsCollection() {
        if (adjustments == null) {
            adjustments = MongoClientManager.getCollection("adjustments", Adjustment.class);
        }
        return adjustments;
    }

    public List getAll() {
        return finAll();
    }

    public BalanceResponse get(BalanceRequest request) {
        BalanceResponse response = new BalanceResponse();
        response.setToken(request.getToken());
        User user = userRepo.find(request.getToken());
        if (user == null) {
            // TODO: 26.03.2019 добавить исключение
            LOGGER.warn("User not found!");
            response.setMessage("User not found!");
            return response;
        }
        Balance balance = getCollection().find(new BasicDBObject("userId", request.getToken())).first();
        if (balance == null) {
            balance = new Balance();
            balance.setBalance(0l);
            balance.setUserId(user.getIdAsString());
            getCollection().insertOne(balance);
        }
        response.setBalance(balance.getBalance());
        LOGGER.debug("Updated " + (balance.getUpdated() != null ? balance.getUpdated().toString() : ""));
        LOGGER.debug(response.toString());
        return response;
    }

    public BalanceResponse adjust(BalanceAdjustRequest request) {
        return Optional.of(new BalanceResponse())
                .map(response -> {
                    response.setToken(request.getToken());
                    return response;
                })
                .map(response -> {
                    Optional.ofNullable(getCollection().find(new BasicDBObject("userId", request.getToken())).first())
                            .map(balance -> {
                                balance.adjust(request.getActivity());
                                getCollection().updateOne(new BasicDBObject("userId", request.getToken()),
                                        new BasicDBObject("$set", new BasicDBObject("balance",
                                                balance.getBalance()).append("updated", new Date())));
                                getAdjustmentsCollection().insertOne(new Adjustment(request.getToken(), request.getActivity()));
                                response.setBalance(balance.getBalance());
                                return balance;
                            })
                            .orElseGet(() -> {
                                // TODO: 27.03.2019
                                LOGGER.error("Balance not found!");
                                response.setMessage("Balance not found!");
                                return null;
                            });
                    return response;
                })
                .map(balanceResponse -> {
                    LOGGER.debug(balanceResponse.toString());
                    return balanceResponse;
                })
                .get();
    }

    public BoardResponse getLiederBoard(BoardRequest request) {
        final BoardResponse response = new BoardResponse();
        final User user = userRepo.find(request.getToken());
        if (user == null) {
            // TODO: 26.03.2019 добавить исключение
            LOGGER.warn("User not found!");
            response.setMessage("User not found!");
            return response;
        }

        final List<Balance> balances = new ArrayList<>();
        final FindIterable<Balance> iterable = getCollection().find(new BasicDBObject("balance", new BasicDBObject("$ne", null))).sort(new BasicDBObject("balance", -1));
        for (Balance balance : iterable) {
            balances.add(balance);
        }

        int i = balances.indexOf(new Balance(request.getToken()));
        if (i < 6) {
            balances.stream()
                    .limit(8)
                    .forEach(balance -> {
                        String login = userRepo.find(balance.getUserId()).getLogin();
                        Long balance1 = balance.getBalance();
                        response.putPosition(login, balance1, balances.indexOf(balance) + 1);
                    });
        } else {
            balances.stream()
                    .limit(3)
                    .forEachOrdered(balance -> response.putPosition(userRepo.find(balance.getUserId()).getLogin(), balance.getBalance(), balances.indexOf(balance) + 1));
            i -= 2;
            for (int j = 0; j < 5; j++) {
                Balance balance = balances.get(i++);
                response.putPosition(userRepo.find(balance.getUserId()).getLogin(), balance.getBalance(), balances.indexOf(balance) + 1);
                if (i > balances.size() - 1) {
                    break;
                }
            }
        }
        return response;
    }

    public boolean doSmth() {
        for (Balance balance : getCollection().find()) {
            if (balance.getUserId() == null) {
                getCollection().deleteOne(
                        new BasicDBObject("_id", balance.getId())
                );
            }
        }
        return true;
    }
}

package server.repo;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.config.MongoClientManager;
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
        BoardResponse response = new BoardResponse();
        User user = userRepo.find(request.getToken());
        if (user == null) {
            // TODO: 26.03.2019 добавить исключение
            LOGGER.warn("User not found!");
            response.setMessage("User not found!");
            return response;
        }

        final List<Balance> balances = new ArrayList<>();
        FindIterable<Balance> iterable = getCollection().find(new BasicDBObject("balance", new BasicDBObject("$ne", null))).sort(new BasicDBObject("balance", -1));
        for (Balance balance : iterable) {
            balances.add(balance);
        }

        final Map<String, Long> liederBoard = new LinkedHashMap<>();

        int i = balances.indexOf(new Balance(request.getToken()));
        if (i < 6) {
            balances.stream()
                    .limit(8)
                    .forEach(balance -> {
                        String login = userRepo.find(balance.getUserId()).getLogin();
                        Long balance1 = balance.getBalance();
                        liederBoard.put(login, balance1);
                    });
        } else {
            balances.stream()
                    .limit(3)
                    .forEachOrdered(balance -> liederBoard.put(userRepo.find(balance.getUserId()).getLogin(), balance.getBalance()));
            i -= 2;
            for (int j = 0; j < 5; j++) {
                Balance balance = balances.get(i++);
                liederBoard.put(userRepo.find(balance.getUserId()).getLogin(), balance.getBalance());
                if (i > balances.size() - 1) {
                    break;
                }
            }
        }
        response.setLiederBoard(liederBoard);
        return response;
    }
}

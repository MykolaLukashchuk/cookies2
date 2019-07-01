package server.repo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.CustomException;
import server.config.MongoClientManager;
import server.core.CheckedFunction;
import server.model.Adjustment;
import server.model.Balance;
import server.model.User;
import server.model.request.BalanceAdjustRequest;
import server.model.request.BalanceRequest;
import server.model.request.BoardRequest;
import server.model.responce.BalanceResponse;
import server.model.responce.BoardResponse;
import server.utils.CustomRuntimeException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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

    private User getUser(String token) throws CustomException {
        User user = userRepo.find(token);
        if (user == null) {
            LOGGER.warn("User not found!");
            throw new CustomException("User not found!");
        }
        return user;
    }

    public BalanceResponse get(BalanceRequest request) throws CustomException {
        BalanceResponse response = new BalanceResponse();
        response.setToken(request.getToken());
        User user = getUser(request.getToken());

        Balance balance = getCollection().find(new BasicDBObject("userId", request.getToken())).first();
        if (balance == null) {
            balance = new Balance();
            balance.setBalance(0L);
            balance.setUserId(user.getIdAsString());
            balance.setUpdated(new Date());
            getCollection().insertOne(balance);
        }
        response.setBalance(balance.getBalance());
        LOGGER.debug("Updated " + (balance.getUpdated() != null ? balance.getUpdated().toString() : ""));
        LOGGER.debug(response.toString());
        return response;
    }

    public Long adjust(String token, Long activity) {
        return Optional.ofNullable(getCollection().find(new BasicDBObject("userId", token)).first())
                .map(wrapper(balance -> {
                    balance.adjust(activity);
                    getCollection().updateOne(new BasicDBObject("userId", token),
                            new BasicDBObject("$set", new BasicDBObject("balance",
                                    balance.getBalance()).append("updated", new Date())));
                    getAdjustmentsCollection().insertOne(new Adjustment(token, activity));
                    return balance;
                }))
                .map(Balance::getBalance)
                .orElseThrow(() -> {
                    throw new CustomRuntimeException("Balance not found!");
                });
    }

        public BalanceResponse adjust(BalanceAdjustRequest request) throws CustomException {
        BalanceResponse response = new BalanceResponse();
        response.setToken(request.getToken());
        User user = getUser(request.getToken());
        try {
            response.setBalance(adjust(request.getToken(), request.getActivity()));
        } catch (CustomRuntimeException e) {
            LOGGER.debug(e.getMessage() + " token: " + request.getToken());
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public BoardResponse getLiederBoard(BoardRequest request) throws CustomException {
        final BoardResponse response = new BoardResponse();
        User user = getUser(request.getToken());

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
                        //todo solve the situation when balance present and user absent
                        String nickname = userRepo.find(balance.getUserId()).getNickname();
                        Long balance1 = balance.getBalance();
                        response.putPosition(nickname, balance1, balances.indexOf(balance) + 1);
                    });
        } else {
            balances.stream()
                    .limit(3)
                    .forEachOrdered(balance -> response.putPosition(userRepo.find(balance.getUserId()).getNickname(), balance.getBalance(),
                            (balances.indexOf(balance) + 1)));
            i -= 2;
            for (int j = 0; j < 5; j++) {
                Balance balance = balances.get(i++);
                response.putPosition(userRepo.find(balance.getUserId()).getNickname(), balance.getBalance(), balances.indexOf(balance) + 1);
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

package server.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.CustomException;
import server.Server;
import server.config.MongoClientManager;
import server.model.ClickersBalance;
import server.model.ClickersBalanceAdjustment;
import server.model.ConfigItem;
import server.model.User;
import server.model.request.AdjustClickersBalanceRequest;
import server.model.request.ClickersBalanceRequest;
import server.model.responce.ClickersBalanceResponse;
import server.model.responce.ClickersPriceResponse;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickersRepo {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClickersRepo.class);
    private final UserRepo userRepo;
    private final ConfigRepo configRepo;
    private final BalanceRepo balanceRepo;
    private MongoCollection<ClickersBalance> collection;
    private MongoCollection<ClickersBalanceAdjustment> adjustments;

    @Inject
    public ClickersRepo(UserRepo userRepo, ConfigRepo configRepo, BalanceRepo balanceRepo) {
        this.userRepo = userRepo;
        this.configRepo = configRepo;
        this.balanceRepo = balanceRepo;
    }

    private MongoCollection<ClickersBalance> getCollection() {
        if (collection == null) {
            collection = MongoClientManager.getCollection("clickersBalance", ClickersBalance.class);
        }
        return collection;
    }

    private MongoCollection<ClickersBalanceAdjustment> getAdjustments() {
        if (adjustments == null) {
            adjustments = MongoClientManager.getCollection("clickersBalanceAdjustments", ClickersBalanceAdjustment.class);
        }
        return adjustments;
    }

    public ClickersBalanceResponse getBalance(ClickersBalanceRequest request) throws CustomException {
        ClickersBalanceResponse response = new ClickersBalanceResponse();
        response.setToken(request.getToken());
        User user = getUser(request.getToken());
        ClickersBalance balance = getClickersBalance(request.getToken());
        response.setClickersBalance(balance.getClickerBalance());
        LOGGER.debug("Updated " + (balance.getUpdated() != null ? balance.getUpdated().toString() : ""));
        LOGGER.debug(response.toString());
        return response;
    }

    private Map<String, Integer> getClickersConfig() {
        try {
            ConfigItem clickers = configRepo.getConfigByKey("clickers");
            return Server.mapper.readValue(clickers.getValue(), Map.class);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private User getUser(String token) throws CustomException {
        User user = userRepo.find(token);
        if (user == null) {
            LOGGER.warn("User not found!");
            throw new CustomException("User not found!");
        }
        return user;
    }

    // TODO: 05.06.2019 Need add work with balance
    public ClickersBalanceResponse adjustBalance(AdjustClickersBalanceRequest request) throws CustomException, IOException {
        ClickersBalanceResponse response = new ClickersBalanceResponse();
        response.setToken(request.getToken());
        User user = getUser(request.getToken());
        ClickersBalance balance = getClickersBalance(request.getToken());

        List<ConfigItem.Clicker> price = Server.mapper.readValue(configRepo.getConfigByKey("clickers").getValue(), new TypeReference<List<ConfigItem.Clicker>>() {});
        ConfigItem.Clicker clicker = price.stream().filter(c -> c.getId().equals(request.getClickerId())).findFirst().get();
        Long clickerBalance = balance.getClickerBalance(request.getClickerId());
        if (clickerBalance == null) {
            clickerBalance = 1L;
        } else {
            clickerBalance += 1L;
        }

        balanceRepo.adjust(request.getToken(), (long)(clicker.getPrice() * Math.pow(2.0, clickerBalance)));


        balance.getClickerBalance().put(request.getClickerId(), clickerBalance);
        getAdjustments().insertOne(new ClickersBalanceAdjustment(request.getToken(), request.getClickerId(), clickerBalance, new Date()));
        getCollection().updateOne(new BasicDBObject("userId", request.getToken()),
                new BasicDBObject("$set", new BasicDBObject("clickerBalance", balance.getClickerBalance())
                        .append("updated", new Date())));
        response.setClickersBalance(balance.getClickerBalance());
        LOGGER.debug("Updated " + (balance.getUpdated() != null ? balance.getUpdated().toString() : ""));
        LOGGER.debug(response.toString());
        return response;
    }

    @Deprecated
    public ClickersPriceResponse getPrice(ClickersBalanceRequest request) throws CustomException {
        ClickersPriceResponse response = new ClickersPriceResponse();
        User user = getUser(request.getToken());
        ClickersBalance balance = getClickersBalance(request.getToken());
        ConfigItem clickerPrice = configRepo.getConfigByKey("clickers");
        response.setMessage(clickerPrice.getValue());
        try {
            // TODO: 01.07.2019
            List list = Server.mapper.readValue(clickerPrice.getValue(), new TypeReference<List<ConfigItem.Clicker>>() {
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return response;
    }

    private ClickersBalance getClickersBalance(String token) {
        ClickersBalance balance = getCollection().find(new BasicDBObject("userId", token)).first();
        if (balance == null) {
            balance = new ClickersBalance();
            HashMap<String, Long> clickers = new HashMap<>();
            balance.setClickerBalance(clickers);
            balance.setUserId(token);
            balance.setUpdated(new Date());
            getCollection().insertOne(balance);
        }
        return balance;
    }
}

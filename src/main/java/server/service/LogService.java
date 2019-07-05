package server.service;

import com.mongodb.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.config.MongoClientManager;
import server.model.BalanceLog;
import server.model.ClickerBalanceLog;

import java.util.Date;
import java.util.Map;

public class LogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogService.class);

    public static LogService getInstance() {
        return new LogService();
    }

    public void cookiesBalanceLog(String userId, Long activity, Long balance, Date date) {
        MongoCollection<BalanceLog> collection = MongoClientManager.getCollection("cookiesBalanceAdjustments", BalanceLog.class);
        BalanceLog log = new BalanceLog(userId, activity, balance, date);
        LOGGER.debug("New cookiesBalanceLog: " + log.toString());
        collection.insertOne(log);
    }

    public void clickerBalanceLog(String userId, Map<String, Long> balance, Date date) {
        MongoCollection<ClickerBalanceLog> collection = MongoClientManager.getCollection("clickersBalanceAdjustments", ClickerBalanceLog.class);
        ClickerBalanceLog log = new ClickerBalanceLog(userId, balance, date);
        LOGGER.debug("New clickerBalanceLog: " + log.toString());
        collection.insertOne(log);
    }
}

package server.repo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.config.MongoClientManager;
import server.model.ConfigItem;
import server.model.request.ConfigRequest;
import server.model.responce.ConfigResponse;

import java.util.HashMap;

public class ConfigRepo {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRepo.class);
    private MongoCollection<ConfigItem> collection;

    private MongoCollection<ConfigItem> getCollection() {
        if (collection == null) {
            collection = MongoClientManager.getCollection("config", ConfigItem.class);
        }
        return collection;
    }

    public ConfigResponse getConfig(ConfigRequest request) {
        HashMap<String, String> configMap = new HashMap<>();
        request.getKeys().forEach(key -> {
            ConfigItem config = getConfigByKey(key);
            if (config != null) {
                configMap.put(key, config.getValue());
            }
        });
        return new ConfigResponse(configMap);
    }

    private ConfigItem getConfigByKey(String key) {
        return getCollection().find(new BasicDBObject("key", key)).first();
    }

    public Boolean putConfig(ConfigItem item) {
        try {
            boolean b = getCollection().find(new BasicDBObject("key", item.getKey())).iterator().hasNext();
            if (b) {
                getCollection().updateOne(new BasicDBObject("key", item.getKey()), new BasicDBObject("$set", new BasicDBObject("value", item.getValue())));
            } else {
                getCollection().insertOne(item);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    public Boolean deleteConfig(ConfigItem request) {
        return getCollection().deleteOne(new BasicDBObject("key", request.getKey())).wasAcknowledged();
    }
}

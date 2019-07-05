package server.repo;

import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.config.MongoClientManager;
import server.model.ConfigItem;
import server.model.request.ConfigRequest;
import server.model.responce.ConfigResponse;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class ConfigRepo {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRepo.class);
    private MongoCollection<ConfigItem> collection;
    private Map<String, ConfigItem> config = new HashMap<>();

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

    public ConfigItem getConfigByKey(String key) {
        ConfigItem item = config.get(key);
        if (item == null) {
            item = getCollection().find(new BasicDBObject("key", key)).first();
        }
        return item;
    }

    public String getConfigValueByKey(String key) {
        return getConfigByKey(key).getValue();
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

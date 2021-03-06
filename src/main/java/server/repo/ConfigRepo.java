package server.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Server;
import server.config.MongoClientManager;
import server.model.ConfigItem;
import server.model.request.ConfigRequest;
import server.model.responce.ConfigResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static server.model.ConfigItem.Clicker;

@Singleton
public class ConfigRepo {
    public static final String CLICKERS = "clickers";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRepo.class);
    private MongoCollection<ConfigItem> collection;
    private Map<String, ConfigItem> config = new HashMap<>();
    private Map<String, Clicker> clickerConfig;

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

    public List<ConfigItem> getAll() {
        List<ConfigItem> list = new ArrayList<>();
        FindIterable<ConfigItem> iterable = getCollection().find();
        for (ConfigItem doc : iterable) {
            list.add(doc);
        }
        return list;
    }

    public Map<String, Clicker> getClickersConfig() {
        if (clickerConfig == null) {
            try {
                clickerConfig = new HashMap<>();
                ((List<Clicker>) Server.mapper.readValue(getConfigByKey(CLICKERS).getValue(), new TypeReference<List<Clicker>>() {
                }))
                        .stream()
                        .forEach(clicker -> clickerConfig.put(clicker.getId(), clicker));
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return clickerConfig;
    }
}

package server.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

public class MongoClientManager {
    private static volatile MongoClientManager INSTANCE = null;
    private final MongoDatabase database;

    public MongoClientManager() {
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("mongodb+srv://user:9zacrvEa23d6bhL@cluster0-b0im4.gcp.mongodb.net/test?retryWrites=true"))
                .codecRegistry(pojoCodecRegistry)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("dev");
    }

    public static MongoClientManager getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new MongoClientManager();
            } catch (MongoException mEx) {
            }
        }
        return INSTANCE;
    }

    public static <T> MongoCollection<T> getCollection(String name, Class<T> className) {
        MongoCollection collection = getInstance().getDatabase().getCollection(name, className);
        return collection;
    }

    public static MongoCollection getCollection(String name) {
        MongoCollection collection = getInstance().getDatabase().getCollection(name);
        return collection;
    }


    public MongoDatabase getDatabase() {
        return database;
    }
}
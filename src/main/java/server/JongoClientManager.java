package server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.POJONode;
import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import com.sun.tools.javac.util.Pair;
import org.jongo.Jongo;
import org.jongo.Mapper;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.JacksonMapper;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class JongoClientManager {

    private static volatile JongoClientManager INSTANCE = null;
    private static Map<String, Object> setup = new HashMap<>();
    private static Set<Class> processedCollections = new HashSet<Class>();
    private Jongo jongo = null;
    private MongoDatabase mongoDatabase;

    private JongoClientManager() throws UnknownHostException, MongoException {

        MongoClientURI uri = new MongoClientURI("mongodb+srv://user:b5UuLvz8FiYf4uWk@cluster0-b0im4.mongodb.net");
        MongoClient mongoClient = new MongoClient(uri);
        DB db = mongoClient.getDB("test");
        jongo = new Jongo(db);
    }

    public static BasicDBObject extractKeyDBObject(JsonNode parse) {

        JsonNode jsonNode = parse.get("key");
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();

        BasicDBObject dbObject = new BasicDBObject();

//        while (fields.hasNext()) {
//            Map.Entry<String, JsonNode> f = (Map.Entry<String, JsonNode>) fields.next();
//            dbObject.append(f.getKey(), Json.fromJson(f.getValue(), Object.class));
//        }

        return dbObject;
    }

    public static JongoClientManager getInstance() {

        if (INSTANCE == null) {
            try {
                INSTANCE = new JongoClientManager();
            } catch (UnknownHostException uhEx) {
            } catch (MongoException mEx) {
            }
        }

        return INSTANCE;
    }

    private Mapper buildMapper() {

        SimpleModule consStringModule = new SimpleModule();

        consStringModule.addDeserializer(Date.class, new DateDeserializers.DateDeserializer() {

            @Override
            public Date deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {

                JsonToken currentToken = parser.getCurrentToken();
                if (currentToken == JsonToken.START_OBJECT) {
                    JsonNode objectNode = parser.readValueAsTree();

                    if (objectNode.has("@ISODate")) {
                        String isoDate = objectNode.get("@ISODate").asText();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        try {
                            Date parse = dateFormat.parse(isoDate);

                            return parse;
                        } catch (ParseException pEx) {
                            throw ctxt.weirdStringException(isoDate, _valueClass, "not a valid representation (error: " + pEx.getMessage() + ")");
                        }
                    }
                }
                if (currentToken == JsonToken.VALUE_EMBEDDED_OBJECT) {
                    JsonNode objectNode = parser.readValueAsTree();
                    if (objectNode.isPojo()) {
                        POJONode pojoNode = (POJONode) objectNode;

                        return (Date) pojoNode.getPojo();
                    }
                    if (objectNode.has("@date")) {
                        String isoDate = objectNode.get("@date").asText();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        try {
                            Date parse = dateFormat.parse(isoDate);

                            return parse;
                        } catch (ParseException pEx) {
                            throw ctxt.weirdStringException(isoDate, _valueClass, "not a valid representation (error: " + pEx.getMessage() + ")");
                        }
                    }
                }

                return super.deserialize(parser, ctxt);
            }
        });

        final Mapper mapper = new JacksonMapper.Builder()
                .registerModule(consStringModule)
                .build();

        return mapper;
    }

    public MongoCollection getCollection(String name) {
        MongoCollection collection = getInstance().jongo.getCollection(name);
        return collection;
    }

    public MongoCollection getCollection(Class<?> clazz) {

        Collection collection = clazz.getAnnotation(Collection.class);
        MongoCollection mongoCollection = getCollection(collection.name());
        if (!collection.alternateKey().isEmpty() && !processedCollections.contains(clazz)) {
            String alternateKey = collection.alternateKey();

            try {
//                mongoCollection.getDBCollection().getIndexInfo().stream().map(index->index.toMap())
                DBCollection dbCollection = mongoCollection.getDBCollection();
//                Map<String, Object> existingIndexes = mongoCollection.getDBCollection().getIndexInfo().stream().map(b -> Pair.of((String) b.get("name"), b.get("key"))).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
//                Json.listFromJson(alternateKey, JsonNode.class).stream()
//                        .map(b -> Pair.of(b.get("name").textValue(), b))
//                        .filter(p -> !existingIndexes.containsKey(p.getKey()))
//                        .forEach(p -> {
//                            JsonNode right = p.getRight();
//                            Map colOptions = Optional.ofNullable(right.get("options")).map(n -> Json.fromJson(n, Map.class)).orElse(Collections.emptyMap());
//
//                            DBObject options = new BasicDBObject(colOptions);
//                            options.put("name", p.getLeft());
//                            options.put("unique", Optional.ofNullable(colOptions.get("unique")).orElse(true));
//                            options.put("background", true);
//                            dbCollection.createIndex(extractKeyDBObject(right), options);
//                        });
            } catch (Exception ex) {
//                LOGGER.error("", ex);
            }

            processedCollections.add(clazz);
//            DBCollection dbCollection = mongoCollection.getDBCollection();
//            Optional<DBObject> findFirst = dbCollection.getIndexInfo().stream().filter(index -> {
//                if (index.containsField(collection.alternateKey())) {
//
//                    return true;
//                }
//
//                return false;
//            }).findFirst();
//            if (!findFirst.isPresent()) {
//                DBObject keys = new BasicDBObject(collection.alternateKey(), -1);
//                dbCollection.createIndex(keys, "ix" + collection.alternateKey(), true);
//            }
        }

        return mongoCollection;
    }

    public Jongo getJongo() {
        return jongo;
    }
}

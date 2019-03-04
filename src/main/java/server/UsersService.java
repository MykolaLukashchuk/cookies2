package server;

import com.mongodb.MongoClient;
import org.jongo.MongoCollection;

public class UsersService {
    private static final JongoClientManager jongoClientManager = JongoClientManager.getInstance();

    private static final MongoCollection testDb = jongoClientManager.getCollection("test");

    public String sayHi() {
        return "Hi!" + testDb.getDBCollection().getFullName();
    }
}

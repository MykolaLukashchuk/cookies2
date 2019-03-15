package server.repo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.types.ObjectId;
import server.config.MongoClientManager;
import server.model.User;
import server.model.request.UserRequest;

import java.util.ArrayList;
import java.util.List;

public class UserRepo {

    private MongoCollection<User> collection;

    public UserRepo() {
    }

    public List finAll() {
        List<User> list = new ArrayList<User>();
        FindIterable<User> iterable = getCollection().find();
        for (User doc : iterable) {
            list.add(doc);
        }
//        try {
//            while (iterable.hasNext()) {
//                list.add(cursor.next().toString());
//            }
//        } finally {
//            cursor.close();
//        }
        return list;
    }

    public void put(User user) {
        getCollection().insertOne(user);
    }

    private MongoCollection<User> getCollection() {
        if (collection == null) {
            collection = MongoClientManager.getCollection("users", User.class);
        }
        return collection;
    }

    public List getAll() {
        return finAll();
    }

    public User auth(UserRequest request) {
        User user = getCollection().find(new BasicDBObject("login", request.getLogin())).first();
        if (user == null) {
            getCollection().insertOne(new User(request.getLogin()));
            user = getCollection().find(new BasicDBObject("login", request.getLogin())).first();
        }
        return user;
    }
}

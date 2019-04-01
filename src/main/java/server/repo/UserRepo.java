package server.repo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.CustomException;
import server.config.MongoClientManager;
import server.model.User;
import server.model.request.UserRequest;
import server.model.responce.UserResponse;

import java.util.ArrayList;
import java.util.List;

public class UserRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepo.class);

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

    public UserResponse auth(UserRequest request) throws CustomException {
        UserResponse response = new UserResponse();

        if (!Character.isLetter(request.getLogin().toCharArray()[0])) {
            throw new CustomException("ERROR. Login can not start with numeric.");
        }
        User user = getCollection().find(new BasicDBObject("login", request.getLogin())).first();
        if (user == null) {
            getCollection().insertOne(new User(request.getLogin()));
            user = getCollection().find(new BasicDBObject("login", request.getLogin())).first();
        }
        response.setToken(user.getIdAsString());
        return response;
    }

    public User find(String token) {
        User user = null;
        try {
            user = getCollection().find(new BasicDBObject("_id", new ObjectId(token))).first();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("User not found");
        }
        return user;
    }
}

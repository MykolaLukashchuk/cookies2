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
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepo.class);

    private MongoCollection<User> collection;

    public UserRepo() {
    }

    public List<User> finAll() {
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

        if (request.getSeed() == null || request.getSeed().equals("")) {
            throw new CustomException("Seed cannot be empty.");
        }

        final UserResponse response = new UserResponse();

        User user = getCollection().find(new BasicDBObject("seed", request.getSeed())).first();

        if (user == null) {
            getCollection().insertOne(new User(request.getSeed()));
            response.setNewDevice(true);
            return response;
        } else if (user.getNickname() == null && request.getNickname() == null) {
            response.setNewDevice(true);
            throw new CustomException("New user's nickname cannot be null.");
        } else if (user.getNickname() == null && request.getNickname() != null) {
            user = getCollection().findOneAndUpdate(new BasicDBObject("seed", request.getSeed()), new BasicDBObject("$set", new BasicDBObject("nickname", request.getNickname()).append("updated", new Date())));
        }
        response.setToken(user.getIdAsString());
        response.setNickname(user.getNickname());
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

    /**
     * "$unset" - delete field
     * "$set" - change field
     * @return
     */
    public boolean doSmth() {
        for (User user : getCollection().find()) {
            if (user.getSeed() == null || user.getSeed().equals("testNewSeed")|| user.getSeed().equals("test4")) {
                getCollection().deleteOne(
                        new BasicDBObject("_id", user.getId())
                );
            }
//                getCollection().updateOne(new BasicDBObject("_id", user.getId()),
//                        new BasicDBObject("$unset", new BasicDBObject("login",
//                                user.getLogin())));
        }
        return true;
    }
}

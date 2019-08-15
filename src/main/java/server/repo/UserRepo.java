package server.repo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.config.MongoClientManager;
import server.model.User;
import server.model.request.BoardRequest;
import server.model.responce.BoardResponse;
import server.utils.CustomRuntimeException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class UserRepo {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepo.class);

    private MongoCollection<User> collection;

    @Inject
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

    public User findUserBySeed(String seed) {
        if (seed == null || seed.equals("")) {
            throw new CustomRuntimeException("Seed cannot be empty.");
        }
        return getCollection().find(new BasicDBObject("seed", seed)).first();
    }

    public User insertNewUser(String seed) {
        getCollection().insertOne(new User(seed));
        return findUserBySeed(seed);
    }

    public void updateUser(User user) {
        getCollection().replaceOne(new BasicDBObject("_id", user.getId()), user);
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

    // TODO: 03.07.2019 временем понадобится оптимизация
    public BoardResponse getLiederBoard(BoardRequest request) {
        final BoardResponse response = new BoardResponse();
        try {
            final List<Balance> balances = new ArrayList<>();
            final FindIterable<User> iterable = getCollection().find(new BasicDBObject("cookiesBalance", new BasicDBObject("$ne", null))).sort(new BasicDBObject("cookiesBalance", -1));
            for (User user : iterable) {
                Balance balance = new Balance(user.getIdAsString(), user.getCookiesBalance(), user.getNickname());
                balances.add(balance);
            }

            int i = balances.indexOf(new Balance(request.getToken(), 0L, ""));
            if (i < 6) {
                balances.stream()
                        .limit(8)
                        .forEach(balance -> response.putPosition(balance.getNickname(), balance.getCookiesBalance(), balances.indexOf(balance) + 1));
            } else {
                balances.stream()
                        .limit(3)
                        .forEachOrdered(balance -> response.putPosition(balance.getNickname(), balance.getCookiesBalance(),
                                (balances.indexOf(balance) + 1)));
                i -= 2;
                for (int j = 0; j < 5; j++) {
                    Balance balance = balances.get(i++);
                    response.putPosition(balance.getNickname(), balance.getCookiesBalance(), balances.indexOf(balance) + 1);
                    if (i > balances.size() - 1) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return response;
    }

    @Getter
    @AllArgsConstructor
    @ToString
    class Balance {
        private String id;
        private Long cookiesBalance;
        private String nickname;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Balance balance = (Balance) o;
            return id.equals(balance.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}

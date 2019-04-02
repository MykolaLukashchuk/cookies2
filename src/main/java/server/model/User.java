package server.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User{
    private ObjectId id;
    private String login;
    private Date created;
    private Date updated;
    private String seed;

    public User(String seed, String  login) {
        this.seed = seed;
        this.login = login;
        created = new Date();
        updated = new Date();
    }

    @JsonGetter ("id")
    public String getIdAsString() {
        return id.toString();
    }
}

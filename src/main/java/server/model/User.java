package server.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User{
    private ObjectId id;
    private String nickname;
    private String login;
    private Date created;
    private Date updated;
    private String seed;

    public User(String seed, String nickname) {
        this.seed = seed;
        this.nickname = nickname;
        created = new Date();
        updated = new Date();
    }

    public User(String seed) {
        this.seed = seed;
        created = new Date();
        updated = new Date();
    }

    @JsonGetter ("id")
    public String getIdAsString() {
        return id.toString();
    }
}

package server.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User{
    private ObjectId id;
    private String login;

    public User(String login) {
        this.login = login;
    }

    @JsonGetter ("id")
    public String getIdAsString() {
        return id.toString();
    }
}

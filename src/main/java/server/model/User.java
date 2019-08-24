package server.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Setter
@Getter
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
    private Long cookiesBalance;
    /**
     * key - id clicker
     * value - clickers balance
     */
    private Map<String, Long> clickerBalance;
    /**
     * key - id clicker
     * value - last time collected clickers
     */
    private Map<String, Date> collectTimes;

    public User(String seed) {
        this.seed = seed;
        created = new Date();
        updated = new Date();
    }

    @JsonGetter ("id")
    public String getIdAsString() {
        return id.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.toString());
    }

    public Map<String, Long> getClickerBalance() {
        if (clickerBalance == null) clickerBalance = new HashMap<>();
        return clickerBalance;
    }

    public Map<String, Date> getCollectTimes() {
        if (collectTimes == null) collectTimes = new HashMap<>();
        return collectTimes;
    }
}

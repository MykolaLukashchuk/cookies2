package server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class ClickerBalanceLog {
    @ToString.Exclude
    private ObjectId id;
    private String userId;
    private Map<String, Long> balance;
    private Date time;

    public ClickerBalanceLog(String userId, Map balance, Date time) {
        this.userId = userId;
        this.balance = balance;
        this.time = time;
    }
}

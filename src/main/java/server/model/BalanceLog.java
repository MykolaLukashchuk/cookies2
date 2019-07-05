package server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class BalanceLog {
    @ToString.Exclude
    private ObjectId id;
    private String userId;
    private Long balance;
    private Long activity;
    private Date time;

    public BalanceLog(String userId, Long activity, Long balance, Date time) {
        this.userId = userId;
        this.activity = activity;
        this.balance = balance;
        this.time = time;
    }
}

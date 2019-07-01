package server.model;


import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import server.CustomException;

import java.util.Date;
import java.util.Objects;

@Data
@NoArgsConstructor
public class Balance {
    private ObjectId id;
    private String userId;
    private Long balance;
    private Date updated;

    public Balance(String userId) {
        this.userId = userId;
    }

    @JsonGetter("id")
    public String getIdAsString() {
        return id.toString();
    }

    public void adjust(Long activity) throws CustomException {
        if ((balance + activity) < 0) {
            throw new CustomException("Not enough balance");
        }
        balance += activity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Balance balance = (Balance) o;
        return userId.equals(balance.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}

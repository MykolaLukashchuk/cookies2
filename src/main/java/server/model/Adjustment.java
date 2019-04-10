package server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Adjustment {
    private ObjectId id;
    private Date date = new Date();
    private Long activity;
    private String userId;

    public Adjustment(String userId, Long activity) {
        this.userId = userId;
        this.activity = activity;
    }
}

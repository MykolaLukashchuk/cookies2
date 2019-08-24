package server.model.responce;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;
@Data
@NoArgsConstructor
public class CollectResponse {
    @NonNull
    private String token;
    private Long balance;
    private String message;
    /**
     * clickers id
     */
    private List<String> collectedClickers;

    public CollectResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public CollectResponse(String token, Long balance, List<String> collectedClickers) {
        this.token = token;
        this.balance = balance;
        this.collectedClickers = collectedClickers;
    }
}

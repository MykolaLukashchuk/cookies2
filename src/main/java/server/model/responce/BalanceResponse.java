package server.model.responce;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import server.model.Balance;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class BalanceResponse {
    private String token;
    private Long balance;
    private String message;

    public BalanceResponse(String token, String message) {
        token = token;
        this.message = message;
    }

    public BalanceResponse(Balance balance) {
        this.balance = balance.getBalance();
        token = balance.getUserId();
    }
}

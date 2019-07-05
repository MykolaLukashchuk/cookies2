package server.model.responce;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BalanceResponse extends BaseResponse {
    private String token;
    private Long balance;
    private String message;

    public BalanceResponse(String token, Long balance) {
        this.token = token;
        this.balance = balance;
    }

    public BalanceResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }
}

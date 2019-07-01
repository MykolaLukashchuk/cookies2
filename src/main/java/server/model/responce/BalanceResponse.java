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
}

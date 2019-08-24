package server.model.responce;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@ToString
public class ClickersBalanceResponse extends BaseResponse {
    @NonNull
    private String token;
    /**
     * (clickerId, count)
     */
    @NonNull
    private Map<String, Long> clickersBalance;
    /**
     * (clickerId, last time collect)
     */
    private Map<String, Date> collectTime;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    public ClickersBalanceResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public ClickersBalanceResponse(String token, Map<String, Long> clickerBalance, Map<String, Date> collectTimes) {
        this.token = token;
        this.clickersBalance = clickerBalance;
        this.collectTime = collectTimes;
    }
}

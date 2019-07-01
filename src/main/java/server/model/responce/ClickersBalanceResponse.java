package server.model.responce;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ClickersBalanceResponse extends BaseResponse {
    private String token;
    private Map<String, Long> clickersBalance;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

}

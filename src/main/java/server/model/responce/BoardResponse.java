package server.model.responce;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class BoardResponse {
    private String token;
    private String message;
    private Map<String, Long> liederBoard;
}

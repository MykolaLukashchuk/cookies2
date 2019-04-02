package server.model.responce;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class UserResponse {
    private String token;
    private String message;
    private String login;

    public UserResponse(String message) {
        this.message = message;
    }
}

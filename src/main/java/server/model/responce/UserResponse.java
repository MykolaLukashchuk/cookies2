package server.model.responce;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserResponse extends BaseResponse {
    private String token;
    private String message;
    private String nickname;
    private Boolean newDevice;

    public UserResponse(String message) {
        this.message = message;
    }
}

package server.model.responce;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ClickersPriceResponse extends BaseResponse {
    private String token;
    private String message;
}

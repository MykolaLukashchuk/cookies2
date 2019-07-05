package server.model.responce;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Response extends BaseResponse{
    private String body;
    private String message;
}

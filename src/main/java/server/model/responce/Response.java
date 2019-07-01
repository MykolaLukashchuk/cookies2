package server.model.responce;

import com.fasterxml.jackson.annotation.JsonInclude;
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

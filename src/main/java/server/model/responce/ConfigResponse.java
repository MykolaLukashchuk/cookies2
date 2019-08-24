package server.model.responce;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ConfigResponse  extends BaseResponse{
    private Map<String,String> config;
}

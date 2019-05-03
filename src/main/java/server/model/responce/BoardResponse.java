package server.model.responce;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class BoardResponse {
    private String token;
    private String message;
    final private List<Position> liederBoard = new ArrayList<>();

    public void putPosition(String nickname, Long balance1, int position) {
        liederBoard.add(new Position(nickname, balance1, position));
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public class Position {
        private String nickname;
        private Long Balance;
        private int Position;
    }
}

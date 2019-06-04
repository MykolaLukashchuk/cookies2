package server.model.responce;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Getter
@Setter
public class BoardResponse {
    private String token;
    private String message;
    private List<Position> liederBoard;

    public void putPosition(String nickname, Long balance1, int position) {
        if (liederBoard == null) {
            liederBoard = new ArrayList<>();
        }
        liederBoard.add(new Position(nickname, balance1, position));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private String nickname;
        private Long balance;
        private int position;
    }
}

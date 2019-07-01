package server.model.responce;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BoardResponse extends BaseResponse {
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
    @ToString
    public static class Position {
        private String nickname;
        private Long balance;
        private int position;
    }
}

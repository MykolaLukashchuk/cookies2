package server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ClickersBalance {
    private String userId;
    private Map<String, Long> clickerBalance;
    private Date updated;

    public Long getClickerBalance(String clickerId) {
        return clickerBalance.get(clickerId);
    }
}


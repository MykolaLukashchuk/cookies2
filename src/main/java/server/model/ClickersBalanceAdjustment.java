package server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClickersBalanceAdjustment {
    private String userId;
    private String clickerId;
    private Long newBalance;
    private Date date;
}

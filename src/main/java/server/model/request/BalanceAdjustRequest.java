package server.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */
@Data
@NoArgsConstructor
public class BalanceAdjustRequest {
    private String token;
    /**
     * Значение на которое меняется баланс
     */
    private Long activity;
}

package server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigItem {
    private String key;
    private String value;

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clicker {
        private String id;
        private String name;
        private Integer price;
        /**
         * Delay in sec.
         */
        private Integer delay;
        private Long benefit;
    }
}

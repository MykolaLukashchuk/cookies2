package server.enums;

import lombok.Getter;

@Getter
public enum ConfigKeys {
    CLICKERS("clickers");

    private String key;

    ConfigKeys(String key) {
        this.key = key;
    }
}

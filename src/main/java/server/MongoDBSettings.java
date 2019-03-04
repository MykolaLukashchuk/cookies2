package server;

public class MongoDBSettings {

    String region;
    String stage;
    boolean local;
    boolean deleteOnStart;
    String port;

    public MongoDBSettings withRegion(String region) {
        this.region = region;
        return this;
    }

    public MongoDBSettings withLocal(boolean value) {
        this.local = value;
        return this;
    }

    public MongoDBSettings withLocalDeleteOnStart(boolean value) {
        this.deleteOnStart = value;
        return this;
    }

    public MongoDBSettings withPort(String port) {
        this.port = port;
        return this;
    }

    public MongoDBSettings withStage(String stage) {
        this.stage = stage;
        return this;
    }
}

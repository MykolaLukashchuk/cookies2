import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class BaseTest {
    final ObjectMapper mapper = new ObjectMapper();
    static final String URL = "http://192.168.0.104:8080";


    protected static  HttpURLConnection getPutHttpURLConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);
        return connection;
    }

    static  HttpURLConnection getPostHttpURLConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);
        return connection;
    }

    protected static  HttpURLConnection getGetHttpURLConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);
        return connection;
    }
}

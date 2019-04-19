import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class BaseTest {
    protected final ObjectMapper mapper = new ObjectMapper();

    protected static  HttpURLConnection getPutHttpURLConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(1000);
        return connection;
    }

    protected static  HttpURLConnection getPostHttpURLConnection(URL url) throws IOException {
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

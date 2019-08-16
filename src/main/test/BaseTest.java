import com.fasterxml.jackson.databind.ObjectMapper;
import server.model.request.Request;
import server.model.request.UserRequest;
import server.model.responce.Response;
import server.model.responce.UserResponse;
import server.utils.EncryptUtils;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class BaseTest {
    static final ObjectMapper mapper = new ObjectMapper();
    //    static final String URL = "http://192.168.0.102:8080";
    static final String URL = "http://192.168.0.101:8080";
//    static final String URL = "http://178.158.212.44:8080";

    protected static  HttpURLConnection getPutHttpURLConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        return connection;
    }

    static  HttpURLConnection getPostHttpURLConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        return connection;
    }

    protected static  HttpURLConnection getGetHttpURLConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        return connection;
    }

    protected static  HttpURLConnection getDeleteHttpURLConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        return connection;
    }

    protected static String login(String seed) throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/users/auth"));

        UserRequest request = new UserRequest();
        request.setSeed(seed);

        String requestString = mapper.writeValueAsString(request);

        mapper.writeValue(connection.getOutputStream(), new Request(EncryptUtils.encryptAsUser(requestString)));
        Response response = mapper.readValue(connection.getInputStream(), Response.class);
        UserResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), UserResponse.class);
        return responseBody.getToken();
    }
}

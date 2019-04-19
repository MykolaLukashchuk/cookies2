import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import server.model.request.UserRequest;
import server.model.responce.UserResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;


public class UsersRouteTest extends BaseTest {

    @Test
    @Ignore
    public void test1() throws IOException {
        HttpURLConnection connection = getGetHttpURLConnection(new URL("http://192.168.0.102:8080/users"));
        String result = new BufferedReader(new InputStreamReader(connection.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void test2() throws IOException {
        HttpURLConnection connection = getPostHttpURLConnection(new URL("http://192.168.0.102:8080/users/auth"));

        UserRequest request = new UserRequest();
        request.setSeed("testNewSeed");

        mapper.writeValue(connection.getOutputStream(), request);
        UserResponse responseBody = mapper.readValue(connection.getInputStream(), UserResponse.class);

        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(responseBody);

        Assert.assertNotNull(responseBody.getNewDevice());
        Assert.assertTrue(responseBody.getNewDevice());
        Assert.assertNull(responseBody.getNickname());
        Assert.assertNull(responseBody.getToken());
        Assert.assertNull(responseBody.getMessage());

        System.out.println(responseBody);
    }

}
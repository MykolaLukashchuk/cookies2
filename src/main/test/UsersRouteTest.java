import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import server.model.request.Request;
import server.model.request.UserRequest;
import server.model.responce.Response;
import server.model.responce.UserResponse;
import server.utils.EncryptUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UsersRouteTest extends BaseTest {

    /**
     * Test of new totally new seed
     * @throws IOException
     */
    @Test
    @Ignore
    public void test2() throws IOException {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/users/auth"));

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

    /**
     * Test of the present user
     * @throws IOException
     */
    @Test
    public void authRealSeed() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/users/auth"));

        UserRequest request = new UserRequest();
        request.setSeed("test4");
//        request.setNickname("Test4");
        System.out.println("Request: " + request.toString());

        Request finalRequest = new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(request)));
        System.out.println("Request: " + finalRequest.toString());
        mapper.writeValue(connection.getOutputStream(), finalRequest);

        Response response = mapper.readValue(connection.getInputStream(), Response.class);

        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getBody());
        Assert.assertNull(response.getMessage());

        UserResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), UserResponse.class);

        Assert.assertNotNull(responseBody);

        Assert.assertNull(responseBody.getNewDevice());
        Assert.assertEquals(responseBody.getNickname(), "Test4");
        Assert.assertEquals(responseBody.getToken(), "5ccc9d00c994ac568669d53a");
        Assert.assertNull(responseBody.getMessage());

        System.out.println(responseBody);
    }

    /**
     * Auth with exception
     *
     * @throws IOException
     */
    @Test
    public void authWithException() throws IOException {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/users/auth"));

        UserRequest request = new UserRequest();
        request.setSeed("");

        String requestString = mapper.writeValueAsString(request);

        mapper.writeValue(connection.getOutputStream(), new Request(EncryptUtils.encryptAsUser(requestString)));

        Response response = mapper.readValue(connection.getInputStream(), Response.class);

        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(response);
        Assert.assertNull(response.getBody());
        Assert.assertNotNull(response.getMessage());

        System.out.println(response.toString());
    }
}
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import server.model.request.BalanceAdjustRequest;
import server.model.request.BalanceRequest;
import server.model.request.BoardRequest;
import server.model.request.Request;
import server.model.responce.BalanceResponse;
import server.model.responce.BoardResponse;
import server.model.responce.Response;
import server.utils.EncryptUtils;

import java.net.HttpURLConnection;
import java.net.URL;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BalanceRouteTest extends BaseTest {

    public static final String TEST_TOKEN = "5ccc9d00c994ac568669d53a";

    @Test
    public void getBalanceTest() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/balance/get"));

        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.setToken("5ccc9d00c994ac568669d53a");

        Request request = new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(balanceRequest)));

        mapper.writeValue(connection.getOutputStream(), request);

        Response response = mapper.readValue(connection.getInputStream(), Response.class);
        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getBody());
        Assert.assertNull(response.getMessage());

        BalanceResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), BalanceResponse.class);

        System.out.println(responseBody.toString());

        Assert.assertNotNull(responseBody);
        Assert.assertNotNull(responseBody.getBalance());
        Assert.assertNotNull(responseBody.getToken());
    }

    @Test
    public void adjustBalanceTest() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/balance/get"));

        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.setToken(TEST_TOKEN);

        mapper.writeValue(connection.getOutputStream(), new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(balanceRequest))));
        Response response = mapper.readValue(connection.getInputStream(), Response.class);

        BalanceResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), BalanceResponse.class);
        System.out.println(responseBody.toString());
        Long firstBalance = responseBody.getBalance();

        BalanceAdjustRequest balanceAdjustRequest = new BalanceAdjustRequest();
        balanceAdjustRequest.setToken(TEST_TOKEN);
        balanceAdjustRequest.setActivity(10L);

        connection = getPostHttpURLConnection(new URL(URL + "/balance/adjust"));
        mapper.writeValue(connection.getOutputStream(), new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(balanceAdjustRequest))));
        response = mapper.readValue(connection.getInputStream(), Response.class);

        responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), BalanceResponse.class);
        System.out.println(responseBody.toString());

        Assert.assertEquals(10L, responseBody.getBalance() - firstBalance);
    }

    @Test
    public void getLiederBoardTest() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/balance/board"));

        BoardRequest boardRequest = new BoardRequest();
        boardRequest.setToken(TEST_TOKEN);

        mapper.writeValue(connection.getOutputStream(), new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(boardRequest))));
        Response response = mapper.readValue(connection.getInputStream(), Response.class);

        BoardResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), BoardResponse.class);
        System.out.println(responseBody.toString());

        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getBody());
        Assert.assertNull(response.getMessage());

    }
}
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import server.model.request.BalanceAdjustRequest;
import server.model.request.BalanceRequest;
import server.model.request.BoardRequest;
import server.model.request.Request;
import server.model.responce.BalanceResponse;
import server.model.responce.BoardResponse;
import server.model.responce.Response;
import server.utils.EncryptUtils;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.net.HttpURLConnection;
import java.net.URL;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BalanceRouteTest extends BaseTest {
    public static final String SEED = "test4";
    public String testToken;

    @Before
    public void init() throws Exception {
        testToken = login(SEED);
    }

    @Test
    public void t1getBalanceTest() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/balance/get"));

        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.setToken(testToken);

        System.out.println("Request: " + balanceRequest.toString());

        Request request = new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(balanceRequest)));
        System.out.println("Request:" + request.toString());

        mapper.writeValue(connection.getOutputStream(), request);

        Response response = mapper.readValue(connection.getInputStream(), Response.class);
        System.out.println(response.toString());
        assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNull(response.getMessage());

        BalanceResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), BalanceResponse.class);

        System.out.println(responseBody.toString());

        assertNotNull(responseBody);
        assertNotNull(responseBody.getBalance());
        assertNotNull(responseBody.getToken());
    }

    @Test
    public void t2adjustBalanceTest() throws Exception {
        long activity = 100000000L;
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/balance/get"));

        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.setToken(testToken);

        mapper.writeValue(connection.getOutputStream(), new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(balanceRequest))));
        Response response = mapper.readValue(connection.getInputStream(), Response.class);

        BalanceResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), BalanceResponse.class);
        System.out.println("Old balance: " + responseBody.toString());
        Long firstBalance = responseBody.getBalance();

        BalanceAdjustRequest balanceAdjustRequest = new BalanceAdjustRequest();
        balanceAdjustRequest.setToken(testToken);
        balanceAdjustRequest.setActivity(activity);

        connection = getPostHttpURLConnection(new URL(URL + "/balance/adjust"));
        mapper.writeValue(connection.getOutputStream(), new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(balanceAdjustRequest))));
        response = mapper.readValue(connection.getInputStream(), Response.class);

        responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), BalanceResponse.class);
        System.out.println("New Balnce" + responseBody.toString());

        assertEquals(activity, responseBody.getBalance() - firstBalance);
    }

    @Test
    public void t3getLiederBoardTest() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/balance/board"));

        BoardRequest boardRequest = new BoardRequest();
        boardRequest.setToken(testToken);

        mapper.writeValue(connection.getOutputStream(), new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(boardRequest))));
        Response response = mapper.readValue(connection.getInputStream(), Response.class);

        System.out.println(response);

        BoardResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), BoardResponse.class);
        System.out.println(responseBody.toString());

        assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNull(response.getMessage());
    }

    @Test
    public void t4adjustBalanceNotEnoughTest() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/balance/get"));

        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.setToken(testToken);

        mapper.writeValue(connection.getOutputStream(), new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(balanceRequest))));
        Response response = mapper.readValue(connection.getInputStream(), Response.class);

        BalanceResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), BalanceResponse.class);
        System.out.println(responseBody.toString());
        Long firstBalance = responseBody.getBalance();

        BalanceAdjustRequest balanceAdjustRequest = new BalanceAdjustRequest();
        balanceAdjustRequest.setToken(testToken);
        balanceAdjustRequest.setActivity(-1 * (firstBalance + 10L));

        connection = getPostHttpURLConnection(new URL(URL + "/balance/adjust"));
        mapper.writeValue(connection.getOutputStream(), new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(balanceAdjustRequest))));
        response = mapper.readValue(connection.getInputStream(), Response.class);

        assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        assertNull(response.getMessage());

        responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), BalanceResponse.class);
        System.out.println(responseBody.toString());

        assertNull(responseBody.getBalance());
        assertEquals(responseBody.getMessage(), "Not enough balance");
        assertEquals(responseBody.getToken(), testToken);
    }
}
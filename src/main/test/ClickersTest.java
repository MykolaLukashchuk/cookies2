import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import server.model.request.ClickersBalanceAdjustRequest;
import server.model.request.ClickersBalanceRequest;
import server.model.request.Request;
import server.model.responce.ClickersBalanceResponse;
import server.model.responce.Response;
import server.utils.EncryptUtils;

import java.net.HttpURLConnection;
import java.net.URL;

public class ClickersTest extends BaseTest {

    public static final String SEED = "test4";
    public String testToken;

    @Before
    public void init() throws Exception {
        testToken = login(SEED);
    }

    @Test
    public void t1getClickersBalance() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/clickers/balance"));
        ClickersBalanceRequest clickerRequest = new ClickersBalanceRequest(testToken);
        Request request = new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(clickerRequest)));
        mapper.writeValue(connection.getOutputStream(), request);

        Response response = mapper.readValue(connection.getInputStream(), Response.class);
        System.out.println("Response: " + response.toString());
        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getBody());
        Assert.assertNull(response.getMessage());

        ClickersBalanceResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), ClickersBalanceResponse.class);

        System.out.println(responseBody.toString());

        Assert.assertNotNull(responseBody);
        Assert.assertNotNull(responseBody.getClickersBalance());
        Assert.assertNotNull(responseBody.getToken());
        Assert.assertEquals(responseBody.getToken(), testToken);
    }

    @Test
    public void t2adjustClickersBalance() throws Exception {
        // TODO: 05.07.2019 Добавить увеличение баланса на достаточный уровень перед тестом
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/clickers/balance"));
        ClickersBalanceRequest clickerRequest = new ClickersBalanceRequest(testToken);
        Request request = new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(clickerRequest)));
        mapper.writeValue(connection.getOutputStream(), request);

        Response response = mapper.readValue(connection.getInputStream(), Response.class);
        System.out.println("Response: " + response.toString());
        ClickersBalanceResponse clickersBalanceResponse = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), ClickersBalanceResponse.class);
        System.out.println("ResponseBody: " + clickersBalanceResponse.toString());

        String key = "2";
        Long startBalance = clickersBalanceResponse.getClickersBalance().get(key);
        if (startBalance == null) startBalance = 0L;


        connection = getPostHttpURLConnection(new URL(URL + "/clickers/adjust"));
        ClickersBalanceAdjustRequest adjustClickersBalanceRequest = new ClickersBalanceAdjustRequest(testToken, key);
        request = new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(adjustClickersBalanceRequest)));
        mapper.writeValue(connection.getOutputStream(), request);

        response = mapper.readValue(connection.getInputStream(), Response.class);
        System.out.println("Response: " + response.toString());
        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getBody());
        Assert.assertNull(response.getMessage());

        ClickersBalanceResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), ClickersBalanceResponse.class);
        System.out.println("ResponseBody: " + responseBody.toString());

        Assert.assertNotNull(responseBody);
        Assert.assertNotNull(responseBody.getClickersBalance());
        Assert.assertNotNull(responseBody.getToken());
        Assert.assertEquals(responseBody.getToken(), testToken);
        Assert.assertEquals(responseBody.getClickersBalance().get(key).intValue(), startBalance + 1);
    }
}

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import server.model.request.AdjustClickersBalanceRequest;
import server.model.request.ClickersBalanceRequest;
import server.model.request.ClickersPriceRequest;
import server.model.request.Request;
import server.model.responce.ClickersBalanceResponse;
import server.model.responce.ClickersPriceResponse;
import server.model.responce.Response;
import server.utils.EncryptUtils;

import java.net.HttpURLConnection;
import java.net.URL;

public class ClickersTest extends BaseTest {

    public static final String TOKEN = "5ccc9d00c994ac568669d53a";

    @Test
    public void t1getClickersBalance() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/clickers/balance"));
        ClickersBalanceRequest clickerRequest = new ClickersBalanceRequest(TOKEN);
        Request request = new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(clickerRequest)));
        mapper.writeValue(connection.getOutputStream(), request);

        Response response = mapper.readValue(connection.getInputStream(), Response.class);
        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getBody());
        Assert.assertNull(response.getMessage());

        ClickersBalanceResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), ClickersBalanceResponse.class);

        System.out.println(responseBody.toString());

        Assert.assertNotNull(responseBody);
        Assert.assertNotNull(responseBody.getClickersBalance());
        Assert.assertNotNull(responseBody.getToken());
        Assert.assertEquals(responseBody.getToken(), TOKEN);
    }

    @Test
    public void t2adjustClickersBalance() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/clickers/balance"));
        ClickersBalanceRequest clickerRequest = new ClickersBalanceRequest(TOKEN);
        Request request = new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(clickerRequest)));
        mapper.writeValue(connection.getOutputStream(), request);

        Response response = mapper.readValue(connection.getInputStream(), Response.class);
        ClickersBalanceResponse clickersBalanceResponse = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), ClickersBalanceResponse.class);
        String key = "2";
        Long startBalance = clickersBalanceResponse.getClickersBalance().get(key);
        if (startBalance == null) startBalance = 0L;


        connection = getPostHttpURLConnection(new URL(URL + "/clickers/adjust"));
        AdjustClickersBalanceRequest adjustClickersBalanceRequest = new AdjustClickersBalanceRequest(TOKEN, key);
        request = new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(adjustClickersBalanceRequest)));
        mapper.writeValue(connection.getOutputStream(), request);

        response = mapper.readValue(connection.getInputStream(), Response.class);
        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getBody());
        Assert.assertNull(response.getMessage());

        ClickersBalanceResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), ClickersBalanceResponse.class);

        System.out.println(responseBody.toString());

        Assert.assertNotNull(responseBody);
        Assert.assertNotNull(responseBody.getClickersBalance());
        Assert.assertNotNull(responseBody.getToken());
        Assert.assertEquals(responseBody.getToken(), TOKEN);
        Assert.assertEquals(responseBody.getClickersBalance().get(key).intValue(), startBalance + 1);
    }

    @Ignore
    @Test
    public void t3GetClickersPrice() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/clickers/price"));
        ClickersPriceRequest clickerRequest = new ClickersPriceRequest(TOKEN);
        Request request = new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(clickerRequest)));
        mapper.writeValue(connection.getOutputStream(), request);

        Response response = mapper.readValue(connection.getInputStream(), Response.class);
        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getBody());
        Assert.assertNull(response.getMessage());

        ClickersPriceResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), ClickersPriceResponse.class);

        System.out.println(responseBody.toString());

        Assert.assertNotNull(responseBody);
        Assert.assertNotNull(responseBody.getToken());
        Assert.assertEquals(responseBody.getToken(), TOKEN);
    }
}

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import server.model.request.BalanceRequest;
import server.model.request.Request;
import server.model.responce.BalanceResponse;
import server.model.responce.Response;
import server.routes.BalanceRoute;
import server.utils.EncryptUtils;

import java.net.HttpURLConnection;
import java.net.URL;

public class ClickersStoreTest extends BaseTest {

    @Test
    public void getPrice() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/clickers/getPrice"));

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
}

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import server.model.ConfigItem;
import server.model.request.ConfigRequest;
import server.model.request.Request;
import server.model.responce.ConfigResponse;
import server.model.responce.Response;
import server.utils.EncryptUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConfigTest extends BaseTest {

    public static final String TEST_KEY = "testKey";
    public static final String TEST_VALUE = "testValue";

    @Test
    public void t1PutNewConfigTest() throws Exception {
        HttpURLConnection connection = getPutHttpURLConnection(new URL(URL + "/config/put"));
        ConfigItem configItem = new ConfigItem(TEST_KEY, TEST_VALUE);
        Request request = new Request(EncryptUtils.encryptAsMaster(mapper.writeValueAsString(configItem)));
        mapper.writeValue(connection.getOutputStream(), request);
        Assert.assertEquals(200, connection.getResponseCode());
    }

    @Test
    public void t2GetConfigTest() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/config/get"));
        ConfigRequest request = new ConfigRequest();
        request.setKeys(Arrays.asList(TEST_KEY));
        Request cryptRequest = new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(request)));
        mapper.writeValue(connection.getOutputStream(), cryptRequest);

        Response response = mapper.readValue(connection.getInputStream(), Response.class);

        Assert.assertEquals(200, connection.getResponseCode());
        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(response);
        Assert.assertNull(response.getMessage());
        Assert.assertNotNull(response.getBody());

        ConfigResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), ConfigResponse.class);

        Assert.assertNotNull(responseBody);
        Assert.assertNotNull(responseBody.getConfig().get(TEST_KEY));
        Assert.assertEquals(responseBody.getConfig().get(TEST_KEY), TEST_VALUE);
        Assert.assertEquals(responseBody.getConfig().size(), 1);
    }

    @Test
    public void t3DelConfigTest() throws Exception {
        HttpURLConnection connection = getPostHttpURLConnection(new URL(URL + "/config/del"));
        ConfigItem configItem = new ConfigItem(TEST_KEY, "");
        Request request = new Request(EncryptUtils.encryptAsMaster(mapper.writeValueAsString(configItem)));
        mapper.writeValue(connection.getOutputStream(), request);
        Assert.assertEquals(200, connection.getResponseCode());

        connection = getPostHttpURLConnection(new URL(URL + "/config/get"));
        ConfigRequest getRequest = new ConfigRequest();
        getRequest.setKeys(Arrays.asList(TEST_KEY));
        Request cryptRequest = new Request(EncryptUtils.encryptAsUser(mapper.writeValueAsString(getRequest)));
        mapper.writeValue(connection.getOutputStream(), cryptRequest);

        Response response = mapper.readValue(connection.getInputStream(), Response.class);

        Assert.assertEquals(200, connection.getResponseCode());
        Assert.assertEquals(connection.getResponseCode(), HttpStatus.SC_OK);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getBody());
        Assert.assertNull(response.getMessage());

        ConfigResponse responseBody = mapper.readValue(EncryptUtils.decryptAsUser(response.getBody()), ConfigResponse.class);

        Assert.assertNotNull(responseBody);
        Assert.assertNull(responseBody.getConfig().get(TEST_KEY));
        Assert.assertEquals(responseBody.getConfig().size(), 0);
    }

}

package server;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.HttpsConnectionContext;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.values.PathMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.model.Group;
import server.repo.GroupRepo;
import server.routes.BalanceRoute;
import server.routes.UsersRoute;

import javax.inject.Inject;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.UUID;

import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import static akka.http.javadsl.model.HttpResponse.create;
import static akka.http.javadsl.server.RequestVals.entityAs;
import static akka.http.javadsl.server.values.PathMatchers.uuid;
import static akka.http.scaladsl.model.StatusCodes.*;

public class Server extends HttpApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

//    public static final java.lang.String PATH = "web\\index.html";
    public static final java.lang.String PATH = "web/index.html";
    private final UsersRoute usersRoute;
    private final BalanceRoute balanceRoute;
    //    public static final java.lang.String PATH = "web/index.html";  //for MAC
    private ObjectMapper mapper = new ObjectMapper();
    private final GroupRepo groups;

    @Inject
    public Server(GroupRepo groups, UsersRoute usersRoute, BalanceRoute balanceRoute) {
        this.groups = groups;
        this.usersRoute = usersRoute;
        this.balanceRoute = balanceRoute;
    }

    public static void main(String[] args) throws IOException {
        LOGGER.info("Start App");
        ActorSystem akkaSystem = ActorSystem.create("akka-http-example");
        Injector injector = Guice.createInjector(new AppModule());
        injector.getInstance(Server.class).bindRoute("0.0.0.0", 8080, akkaSystem);
        final Http http = Http.get(akkaSystem);

//        boolean useHttps = false; // pick value from anywhere
//        if ( useHttps ) {
//            HttpsConnectionContext https = useHttps(akkaSystem);
//            http.setDefaultClientHttpsContext();
//        }

        System.out.println("<ENTER> to exit!");
        System.in.read();
        akkaSystem.shutdown();
    }

    @Override
    public Route createRoute() {

        PathMatcher<UUID> uuidExtractor = uuid();

        return handleExceptions(e -> {
                    e.printStackTrace();
                    return complete(create().withStatus(InternalServerError()));
                },
                pathSingleSlash().route(
                        getFromResource(PATH)
                ),
                pathPrefix("groups").route(
                        get(pathEndOrSingleSlash().route(
                                handleWith(ctx -> ctx.completeAs(Jackson.json(), groups.getAll()))
                        )),
                        get(path(uuidExtractor).route(
                                handleWith(uuidExtractor,
                                        (ctx, uuid) -> ctx.completeAs(Jackson.json(), groups.get(uuid))
                                )
                        )),
                        post(
                                handleWith(entityAs(jsonAs(Group.class)),
                                        (ctx, group) -> {
                                            Group saved = groups.create(group);
                                            return
                                                    ctx.complete(HttpResponse.create()
                                                            .withStatus(Created())
                                                            .addHeader(
                                                                    Location.create(
                                                                            Uri.create("http://localhost:8080/groups/" + saved.getUuid()))));
                                        }
                                )
                        ),
                        put(path(uuidExtractor).route(
                                handleWith(uuidExtractor, entityAs(jsonAs(Group.class)),
                                        (ctx, uuid, group) -> {
                                            if (!Objects.equals(group.getUuid(), uuid))
                                                return ctx.completeWithStatus(BadRequest());
                                            else {
                                                groups.update(group);
                                                return ctx.completeWithStatus(OK());
                                            }
                                        }
                                )
                        )),
                        put(path(uuidExtractor).route(
                                handleWith(uuidExtractor,
                                        (ctx, uuid) -> {
                                            groups.delete(uuid);
                                            return ctx.completeWithStatus(OK());
                                        }
                                )
                        ))
                ),
                usersRoute.getRoute(),
                balanceRoute.getRoute()
        );
    }

    //#https-http-config
    // ** CONFIGURING ADDITIONAL SETTINGS ** //

    public static HttpsConnectionContext useHttps(ActorSystem system) {
        HttpsConnectionContext https = null;
        try {
            // initialise the keystore
            // !!! never put passwords into code !!!
            final char[] password = new char[]{'a', 'b', 'c', 'd', 'e', 'f'};

            final KeyStore ks = KeyStore.getInstance("PKCS12");
            final InputStream keystore = Server.class.getClassLoader().getResourceAsStream("httpsDemoKeys/keys/server.p12");
            if (keystore == null) {
                throw new RuntimeException("Keystore required!");
            }
            ks.load(keystore, password);

            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(ks, password);

            final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            https = ConnectionContext.https(sslContext);

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            system.log().error("Exception while configuring HTTPS.", e);
        } catch (CertificateException | KeyStoreException | UnrecoverableKeyException | IOException e) {
            system.log().error("Exception while ", e);
        }

        return https;
    }
    //#https-http-config
}

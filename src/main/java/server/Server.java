package server;

import akka.actor.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Uri;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.values.PathMatcher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import server.model.Group;
import server.model.User;
import server.model.request.UserRequest;
import server.repo.GroupRepo;
import server.repo.UserRepo;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import static akka.http.javadsl.model.HttpResponse.create;
import static akka.http.javadsl.server.RequestVals.entityAs;
import static akka.http.javadsl.server.values.PathMatchers.uuid;
import static akka.http.scaladsl.model.StatusCodes.*;

public class Server extends HttpApp {

    public static final java.lang.String PATH = "web\\index.html";
    //    public static final java.lang.String PATH = "web/index.html";  //for MAC
    private ObjectMapper mapper = new ObjectMapper();
    private final GroupRepo groups;
    private final UserRepo userRepo;

    @Inject
    public Server(GroupRepo groups, UserRepo userRepo) {
        this.groups = groups;
        this.userRepo = userRepo;
    }

    public static void main(String[] args) throws IOException {
        ActorSystem akkaSystem = ActorSystem.create("akka-http-example");
        Injector injector = Guice.createInjector(new AppModule());
        injector.getInstance(Server.class).bindRoute("0.0.0.0", 8080, akkaSystem);

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
                pathPrefix("users").route(
                        get(pathEndOrSingleSlash().route(
                                handleWith(requestContext -> requestContext.completeAs(Jackson.json(), userRepo.getAll()))
                        )),
                        pathSuffix("auth").route(
                                post(pathEndOrSingleSlash()
                                        .route(
                                                handleWith(entityAs(jsonAs(UserRequest.class)),
                                                        (ctx, user) -> {
                                                            User auth = userRepo.auth(user);
                                                            return ctx.completeAs(Jackson.json(), auth);
                                                        }
                                                )
                                        )
                                )
                        )

                )
        );
    }
}

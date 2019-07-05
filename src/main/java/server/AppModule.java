package server;

import com.google.inject.AbstractModule;
import server.config.ActorHandler;
import server.routes.BalanceRoute;
import server.routes.ClickersRoute;
import server.routes.ConfigRoute;
import server.routes.UsersRoute;

public class AppModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Server.class);
    bind(UsersRoute.class);
    bind(BalanceRoute.class);
    bind(ConfigRoute.class);
    bind(ClickersRoute.class);
      bind(ActorHandler.class);
  }
}
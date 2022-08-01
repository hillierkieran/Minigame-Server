package minigames.server;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import minigames.rendering.GameMetadata;


public class MinigameNetworkServer {

    /** A logger for logging output */
    private static final Logger logger = LogManager.getLogger(MinigameNetworkServer.class);

    Vertx vertx;
    HttpServer server;
    Router router;

    public MinigameNetworkServer(Vertx vertx) {
        this.vertx = vertx;
        this.server = vertx.createHttpServer();
        this.router = Router.router(vertx);
    }

    /** Starts the server on the given port */
    public void start(int port) {
        router.route().handler(BodyHandler.create());

        // A basic ping route to check if there is contact
        router.get("/ping").handler((ctx) -> {
            ctx.response().end("pong");
        });

        // Gets the list of game servers for this client type
        router.get("/gameServers/:clientType").respond((ctx) -> {
          String clientStr = ctx.pathParam("clientType");
          ClientType ct = ClientType.valueOf(clientStr);
          List<GameServer> servers = Main.gameRegistry.getGamesForPlatform(ct);

          /** Vertx/Jackson should turn this into a JSON list, because we're just outputing a simple List<record> */
          return Future.succeededFuture(servers.stream().map((gs) -> gs.getDetails()).toList());
        });

        // Gets the list of game servers for this client type
        router.get("/games/:gameServer").respond((ctx) -> {
          String serverName = ctx.pathParam("gameServer");
          GameServer gs = Main.gameRegistry.getGameServer(serverName);
          GameMetadata[] games = gs.getGamesInProgress();

          /** Vertx/Jackson should turn this into a JSON list, because we're just outputing a simple List<record> */
          return Future.succeededFuture(Arrays.asList(games));
        });

        server.requestHandler(router).listen(port, (http) -> {
            if (http.succeeded()) {
              logger.info("Server started on {}", port);
            } else {
              logger.error("Server failed to start");
            }
          });
    }
   



}

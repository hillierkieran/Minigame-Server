package minigames.server;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import minigames.commands.CommandPackage;
import minigames.rendering.GameMetadata;
import minigames.rendering.RenderingPackage;


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
        router.route()
          .handler(CorsHandler.create().allowedMethod(HttpMethod.POST))
          .handler(BodyHandler.create());

        /* Force CORS to respond that everything is ok
        router.options().handler((ctx) -> {
          ctx.response().putHeader("Acces-Control-Allow-Origin", "*");
          ctx.response().end("");
        });*/

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


        // Starts a new game on the server
        router.post("/newGame/:gameServer").respond((ctx) -> {
          String serverName = ctx.pathParam("gameServer");
          GameServer gs = Main.gameRegistry.getGameServer(serverName);

          String playerName = ctx.body().asString();

          /*
           * executeBlocking moves this onto a background thread
           */
          Future<RenderingPackage> resp = vertx.executeBlocking((promise) -> gs.newGame(playerName).onSuccess((r) -> {
            logger.info("package {}", r);
            promise.complete(r);
          }));
          return resp;
        });

        // Starts a new game on the server
        router.post("/joinGame/:gameServer/:game").respond((ctx) -> {
          String serverName = ctx.pathParam("gameServer");
          String gameName = ctx.pathParam("game");
          GameServer gs = Main.gameRegistry.getGameServer(serverName);

          String playerName = ctx.body().asString();

          /*
            * executeBlocking moves this onto a background thread
            */
          Future<RenderingPackage> resp = vertx.executeBlocking((promise) -> gs.joinGame(gameName, playerName).onSuccess((r) -> {
            logger.info("package {}", r);
            promise.complete(r);
          }));
          return resp;
        });
        
        // Sends a command package to a game on the server
        router.post("/command").respond((ctx) -> {
          JsonObject data = ctx.body().asJsonObject();
          CommandPackage cp = CommandPackage.fromJson(data);

          GameServer gs = Main.gameRegistry.getGameServer(cp.gameServer());

          /*
            * executeBlocking moves this onto a background thread
            */
          Future<RenderingPackage> resp = vertx.executeBlocking((promise) -> gs.callGame(cp).onSuccess((r) -> {
            logger.info("package {}", r);
            promise.complete(r);
          }));
          return resp;
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

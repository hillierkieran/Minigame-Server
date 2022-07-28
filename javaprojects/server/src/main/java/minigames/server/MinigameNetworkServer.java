package minigames.server;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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
        router.get("/ping").handler((ctx) -> {
            ctx.response().end("pong");
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

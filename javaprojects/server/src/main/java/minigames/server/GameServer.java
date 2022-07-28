package minigames.server;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

public class GameServer {

    Vertx vertx;
    HttpServer server;
    Router router;

    public GameServer(Vertx vertx) {
        this.vertx = vertx;
        this.server = vertx.createHttpServer();
        this.router = Router.router(vertx);
    }

    public void start() {
        
    }
   



}

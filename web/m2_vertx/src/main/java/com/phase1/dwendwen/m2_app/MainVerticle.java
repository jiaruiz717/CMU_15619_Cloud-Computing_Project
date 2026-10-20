package com.phase1.dwendwen.m2_app;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    // vertx.deployVerticle(new EncodeVerticle());
    // vertx.deployVerticle(new DecodeVerticle());
    DeploymentOptions options = new DeploymentOptions().setInstances(4);
    vertx.deployVerticle("com.phase1.dwendwen.m2_app.BlockChainVerticle", options);

    // create a router
    Router router = Router.router(vertx);
    router.get("/blockchain").handler(this::blockChainDispatcher); // passing in dispatcher function
    vertx.createHttpServer().requestHandler(router).listen(8080).onSuccess(server -> {
      System.out.println("listening on " + server.actualPort());
    });
  }

  void blockChainDispatcher(RoutingContext ctx) {
    MultiMap queryParams = ctx.queryParams();
    String cc = (queryParams.contains("cc")) ? queryParams.get("cc") : "error";
    // 'data' or 'type' field is missing in query
    if ("error".equals(cc)) {
      ctx.request().response().end();
      return;
    }
//    if ("encode".equals(queryType)) {
//      vertx.eventBus().request("encode.vertx", queryPayload, reply -> {
//        ctx.request().response().end((String) reply.result().body());
//      });
//    } else if ("decode".equals(queryType)) {
//      vertx.eventBus().request("decode.vertx", queryPayload, reply -> {
//        ctx.request().response().end((String) reply.result().body());
//      });
//    }
    vertx.eventBus().request("blockchain.vertx", cc, reply -> {
      ctx.request().response().end((String) reply.result().body());
    });
  }

}

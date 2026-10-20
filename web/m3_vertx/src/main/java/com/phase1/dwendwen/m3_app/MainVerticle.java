package com.phase1.dwendwen.m3_app;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.Arrays;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    // vertx.deployVerticle(new EncodeVerticle());
    // vertx.deployVerticle(new DecodeVerticle());
    DeploymentOptions options = new DeploymentOptions().setInstances(4);
    vertx.deployVerticle("com.phase1.dwendwen.m3_app.TwitterVerticle", options);

    // create a router
    Router router = Router.router(vertx);
    router.get("/twitter").handler(this::twitterDispatcher); // passing in dispatcher function
    vertx.createHttpServer().requestHandler(router).listen(8080).onSuccess(server -> {
      System.out.println("listening on " + server.actualPort());
    });
  }

  void twitterDispatcher(RoutingContext ctx) {
    MultiMap queryParams = ctx.queryParams();
    Long id = (queryParams.contains("type")) ? Long.parseLong(queryParams.get("user_id")) : -1;
    String type = (queryParams.contains("type")) ? queryParams.get("type") : "error";
    String phrase = (queryParams.contains("phrase")) ? queryParams.get("phrase") : "error";
    String hashtag = (queryParams.contains("hashtag")) ? queryParams.get("hashtag") : "error";
    // 'data' or 'type' field is missing in query
    if ("error".equals(type) || "error".equals(phrase) || "error".equals(hashtag)) {
      ctx.request().response().end();
      return;
    }
    JsonObject input = new JsonObject();
    input.put("id",id);
    input.put("type",type);
    input.put("phrase",phrase);
    input.put("hashtag",hashtag);
//    if ("encode".equals(queryType)) {
//      vertx.eventBus().request("encode.vertx", queryPayload, reply -> {
//        ctx.request().response().end((String) reply.result().body());
//      });
//    } else if ("decode".equals(queryType)) {
//      vertx.eventBus().request("decode.vertx", queryPayload, reply -> {
//        ctx.request().response().end((String) reply.result().body());
//      });
//    }
    vertx.eventBus().request("twitter.vertx", input, reply -> {
      ctx.request().response().end((String) reply.result().body());
    });
  }

}

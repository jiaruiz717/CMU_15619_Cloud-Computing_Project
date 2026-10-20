package com.phase1.dwendwen.mix_app;

import TwitterHandling.Data;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class TwitterVerticle extends AbstractVerticle {
    @Override
    public void start() {
        vertx.eventBus().consumer("twitter.vertx", msg -> {
            JsonObject payload = (JsonObject) msg.body();
            System.out.println(payload);
            long id = payload.getLong("id");
            String type = payload.getString("type");
            String phrase = payload.getString("phrase");
            String hashtag = payload.getString("hashtag");
            if (!(type.equals("reply") || type.equals("retweet") || type.equals("both"))) {
                msg.reply("dwendwen,144331934093\nINVALID");
            }
            msg.reply(Data.serve(id, type, phrase, hashtag));
        });
    }
}

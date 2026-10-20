package com.phase1.dwendwen.m2_app;

import com.phase1.dwendwen.m2_app.blockChainHandling.Service;
import io.vertx.core.AbstractVerticle;

public class BlockChainVerticle extends AbstractVerticle {
    @Override
    public void start() {
        vertx.eventBus().consumer("blockchain.vertx", msg -> {
            String payload = (String) msg.body();
//      msg.reply(String.format("payload is %s", payload));
            msg.reply(Service.serve(payload));
        });
    }
}

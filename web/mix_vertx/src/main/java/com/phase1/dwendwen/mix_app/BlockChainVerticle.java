package com.phase1.dwendwen.mix_app;

import blockChainHandling.Service;
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

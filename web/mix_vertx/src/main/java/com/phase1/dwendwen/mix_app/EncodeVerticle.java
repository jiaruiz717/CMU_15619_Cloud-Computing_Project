package com.phase1.dwendwen.mix_app;

import io.vertx.core.AbstractVerticle;
import qrHandling.QREncoding;

public class EncodeVerticle extends AbstractVerticle{
    @Override
    public void start(){
        vertx.eventBus().consumer("encode.vertx", msg -> {
            String payload = (String) msg.body();
            QREncoding encoding = new QREncoding();
//      msg.reply(String.format("payload is %s, encoding is %s", payload, encoding.getEncoding(payload)));
            msg.reply(String.format(encoding.getEncoding(payload)));
        });
    }
}

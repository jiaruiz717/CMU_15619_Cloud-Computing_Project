package com.phase1.dwendwen.mix_app;


import io.vertx.core.AbstractVerticle;
import qrHandling.QRDecoding;

public class DecodeVerticle extends AbstractVerticle{
    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("decode.vertx", msg -> {
            String payload = (String) msg.body();
            QRDecoding decoding = new QRDecoding(payload);
            msg.reply(String.format(decoding.processInputHex()));
        });
    }
}

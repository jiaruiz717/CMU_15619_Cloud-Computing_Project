package com.phase1.dwendwen.m1_app;

import com.phase1.dwendwen.m1_app.qrHandling.QRDecoding;
import io.vertx.core.AbstractVerticle;

public class DecodeVerticle extends AbstractVerticle {
  @Override
  public void start() throws Exception {
    vertx.eventBus().consumer("decode.vertx", msg -> {
      String payload = (String) msg.body();
      QRDecoding decoding = new QRDecoding(payload);
      msg.reply(String.format(decoding.processInputHex()));
    });
  }
}

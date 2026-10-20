package com.phase1.dwendwen.m1_app;

import com.phase1.dwendwen.m1_app.qrHandling.QREncoding;
import io.vertx.core.AbstractVerticle;

public class EncodeVerticle extends AbstractVerticle {
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

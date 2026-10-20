package app;

import io.jooby.Jooby;

public class App extends Jooby {
  {
    get("/qrcode", ctx -> {
      String type = ctx.query("type").value();
      String data = ctx.query("data").value();
      if (type.equals("encode")) {
        return new QREncoding().getEncoding(data);
      } else if (type.equals("decode")) {
        return new QRDecoding(data).processInputHex();
      }
      return "No such type!";
    });
  }

  public static void main(final String[] args) {
    runApp(args, App::new);
  }

}

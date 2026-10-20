package app;

import io.jooby.Jooby;

public class App extends Jooby {
  //private static final Cache cache = new Cache();
  {
    get("/twitter", ctx -> {
      try {
        long id = Long.parseLong(ctx.query("user_id").value());
        String type = ctx.query("type").value();
        String phrase = ctx.query("phrase").value();
        String hashtag = ctx.query("hashtag").value();
        if (!(type.equals("reply") || type.equals("retweet") || type.equals("both"))) {
          return "dwendwen,144331934093\nINVALID";
        }
        return Data.serve(id, type, phrase, hashtag);
      } catch (Exception e) {
        return e.getStackTrace();
      }
    });

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

    get("/blockchain", ctx -> {
      String cc = ctx.query("cc").value();
      return Service.serve(cc);
    });

    get("/", ctx -> "Welcome!");
  }

  public static void main(final String[] args) {
    runApp(args, App::new);
  }

}

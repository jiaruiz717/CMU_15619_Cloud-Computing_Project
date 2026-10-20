package app;

import io.jooby.Jooby;

public class App extends Jooby {
  {
    get("/blockchain", ctx -> {
      String cc = ctx.query("cc").value();
      return Service.serve(cc);
    });
  }

  public static void main(final String[] args) {
    runApp(args, App::new);
  }

}

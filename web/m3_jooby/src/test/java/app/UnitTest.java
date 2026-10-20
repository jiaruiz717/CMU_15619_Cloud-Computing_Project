package app;

import io.jooby.JoobyTest;
import io.jooby.StatusCode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JoobyTest(App.class)
public class UnitTest {

//  static OkHttpClient client = new OkHttpClient();
//
//  @Test
//  public void serve(int serverPort) throws IOException {
//    String path = "/";
//    Request req = new Request.Builder()
//        .url("http://localhost:" + serverPort + path)
//        .build();
//
//    try (Response rsp = client.newCall(req).execute()) {
//      assertEquals("Welcome!", Objects.requireNonNull(rsp.body()).string());
//      assertEquals(StatusCode.OK.value(), rsp.code());
//    }
//  }
}

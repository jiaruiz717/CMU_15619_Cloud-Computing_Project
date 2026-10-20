package app;

import io.jooby.JoobyTest;
import io.jooby.StatusCode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JoobyTest(App.class)
public class UnitTest {

  static OkHttpClient client = new OkHttpClient();

  @Test
  public void encode(int serverPort) throws IOException {
    String path = "/qrcode?type=encode&data=CC%20Team";
    Request req = new Request.Builder()
        .url("http://localhost:" + serverPort + path)
        .build();

    try (Response rsp = client.newCall(req).execute()) {
      assertEquals("0x66d92b800x5bc76d830x121a7fa60x51c111870x3a5f3ca30x8be36a130xedb223a0xfc8e98780x33bf50de0x2e8709700x545a2d0f0xecef7ae0x461175cd0xff132a", rsp.body().string());
      assertEquals(StatusCode.OK.value(), rsp.code());
    }
  }

  @Test
  public void decode(int serverPort) throws IOException {
    String path = "/qrcode?type=decode&data=0x2b23d6830x15a0de0d0x744784010x29e880700xfe1adf5c0xb96061290x1127b67c0x311690430xc63153140xf6e00650x92d3960b0xf59a79070x704e73d40x977fd8090xf516e98a0x3e0c19f10xac626d040x6a3e58650xca85aa3e0x6266b640x842ddcb40x4e7c879c0x85dd21240x3afae3dc0xe07908a70x664685970xb38246f70x511908330x40a111ee0xc12c8fd10x82984c520x4ddee6f6";
    Request req = new Request.Builder()
            .url("http://localhost:" + serverPort + path)
            .build();

    try (Response rsp = client.newCall(req).execute()) {
      assertEquals("CC Team is awesome!", rsp.body().string());
      assertEquals(StatusCode.OK.value(), rsp.code());
    }
  }
}

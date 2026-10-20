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

  static OkHttpClient client = new OkHttpClient();

  @Test
  public void serve(int serverPort) throws IOException {
    String path = "/blockchain?cc=eJyFk9tum0AQht9lr7mYw85heZWqigBDbClxqxg1lSK_ewcCBddJuldoF3a--f7hLXXH5nRO9be31Dw9PYy_58eXvvuVai-SRd3Ji1WpeR5TLbCsKo2n5z7VCcVJCDJsK1Xp2FyOcZhbMnOFdP1epZ8_XmNrOj0dUg1_XwLrilu2OBibl8d-nG5N1-qG6HJ6TDUKcRCwBVepVkpGViRAp9gb-oCiDL4AI6lnL1nvgKc7TAiLaVS-9OfDvx0vfB1k46Y9zEjvNZUwxx1MbOU_ZhCA0OMGl4KbmaYVHfDgOzMEtLjBzQ2IDAeUvRv4WE60D0ScobCtbgqIFQgHBVc35kiyMGc3LYT3xIWyUg4g2dTcaF7z1VZx8H4GmimoCDmia5kieKdAKOY5JiSMrRiwIDgbSfF7BlSPtDFagq_jabXDFmHYxXPb9lyH5JN4FBhIVQ2Ut3haMAn1sosH13Rolw65N-w36TTzN-f-9WHbg2m45639NEfpQgaCRKsqFVcjN2bgxRQL69IEZ1K2fCdLWZkMgxf168BwaLN3hjtZH_zln9bJYjm7ksI0uNc_g4cQeA==";
    Request req = new Request.Builder()
        .url("http://localhost:" + serverPort + path)
        .build();

    try (Response rsp = client.newCall(req).execute()) {
      assertEquals("dwendwen,144331934093\neJyFk9tum0AUAP9ln3k4lz2X5VeqqgIMsaUkrWLUVor87z12IIDtuDwhFnZnZ5b31O2bw2uqv72n5vn5x_j3cvvWd79T7UWyqDt5sSo1L2OqBaarSuPhpU91QnESggzLlaq0b477GMwtmblCOn2v0q-ff-LRefSwSzV8vgTWFbdsMTA2b0_9eJ41naoN0fHwlGoU4iBgC65SzZSMrEiATvFs6AOKMvgEjKSevWS9AT7PYUJYTGPlY_-6u97xxNdBNm7a3QXpY00lzDEHE1v5jxkEIPSYwaXgYqZpRQfc-coMAU1ucHEDIsMOZe0G7suJ7QMRZyhss5sCYgXCQcHZjTmSTMzZTQvhLXGhrJQDSBY1G81zX20VB-8vQBcKKkKO6FrOCT4oEIp5jhMSxmYMmBCcjaT4LQOqR22MLcHjPK122CIMqzzbbV_WIfkijwIDqaqB8pKnBZNQL6s8ONehVR1yb9g3dZr7RzfWKWQgSDR7UXE1cmMGnrSwsE7EnEnZ8o0ZZWUyDDjUx3VwaLN3hksdjD-DYqOhFbG6-5tv6nzJkMVydiUF_2S4yjwfdJIWh66s8ly9-LBP5L_XB_uGuHhe9QmTUyBeBUIr0nabQGDx1ekff489yA==",
              Objects.requireNonNull(rsp.body()).string());
      assertEquals(StatusCode.OK.value(), rsp.code());
    }
  }
}

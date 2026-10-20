package app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class IOFormatProcess {

    public static String decode(String data) {
        byte[] input = Base64.getUrlDecoder().decode(data);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);
        Inflater decompressor = new Inflater();
        try {
            decompressor.setInput(input);
            final byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
        } catch (DataFormatException e) {
            e.printStackTrace();
        } finally {
            decompressor.end();
        }
        return bos.toString();
    }

    public static String encode(String data) {
        byte[] input = data.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Deflater compressor = new Deflater();
        compressor.setInput(input);
        compressor.finish();
        byte[] buffer = new byte[1024];
        while (!compressor.finished()) {
            int count = compressor.deflate(buffer); // returns the generated code... index
            bos.write(buffer, 0, count);
        }
        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.getUrlEncoder().encodeToString(bos.toByteArray());
    }

}

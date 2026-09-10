package cz.logicgo.core.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compression {

    private static final byte FLAG_RAW = 0x00;
    private static final byte FLAG_COMPRESSED = 0x01;

    private static final int BUFFER_SIZE = 2048;
    private static final int MIN_SIZE_TO_COMPRESS = 128;

    public static byte[] compress(byte[] input) {
        if (input == null || input.length == 0) return input;

        if (input.length < MIN_SIZE_TO_COMPRESS) {
            byte[] result = new byte[input.length + 1];
            result[0] = FLAG_RAW;
            System.arraycopy(input, 0, result, 1, input.length);
            return result;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        int dynamicLevel = getCompressionLevel(input.length);

        try {
            try (GZIPOutputStream gzip = new CustomGZIPOutputStream(baos, dynamicLevel)) {
                gzip.write(input);
            }

            byte[] compressed = baos.toByteArray();

            if (compressed.length >= input.length) {
                byte[] result = new byte[input.length + 1];
                result[0] = FLAG_RAW;
                System.arraycopy(input, 0, result, 1, input.length);
                return result;
            }

            byte[] result = new byte[compressed.length + 1];
            result[0] = FLAG_COMPRESSED;
            System.arraycopy(compressed, 0, result, 1, compressed.length);
            return result;

        } catch (IOException e) {
            return input;
        }
    }

    private static int getCompressionLevel(int dataSizeInBytes) {
        if (dataSizeInBytes < 1024 * 64) {
            return Deflater.DEFAULT_COMPRESSION;
        } else if (dataSizeInBytes < 1024 * 1024) {
            return 4;
        } else {
            return Deflater.BEST_SPEED;
        }
    }

    public static byte[] decompress(byte[] payload) {
        if (payload == null || payload.length == 0) return payload;

        byte flag = payload[0];

        if (flag == FLAG_RAW) {
            return Arrays.copyOfRange(payload, 1, payload.length);
        }

        if (flag == FLAG_COMPRESSED) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(payload, 1, payload.length - 1);
                 GZIPInputStream gis = new GZIPInputStream(bais, BUFFER_SIZE);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream(payload.length * 4)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int len;
                while ((len = gis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                return baos.toByteArray();
            } catch (IOException e) {
                return payload;
            }
        }

        return payload;
    }

    private static class CustomGZIPOutputStream extends GZIPOutputStream {
        public CustomGZIPOutputStream(java.io.OutputStream out, int level) throws IOException {
            super(out);
            def.setLevel(level);
        }
    }
}

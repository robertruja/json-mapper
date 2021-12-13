package org.crumbs.json;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
    private static byte[] readResourceBytes(String resource) {
        return readInputStream(FileUtil.class.getClassLoader().getResourceAsStream(resource));

    }

    public static String readResource(String name) {
        return new String(readResourceBytes(name));
    }

    private static byte[] readInputStream(InputStream is) {
        try {
            int max_read = 1024;
            int totalRead = 0;

            byte[] buffer = new byte[2 * max_read];
            int offset = 0;

            while (true) {
                int read = is.read(buffer, offset, max_read);
                totalRead += read;
                offset = offset + max_read;
                if (read == max_read) {
                    if (offset == buffer.length) {
                        // ensure capacity
                        byte[] doubleBuffer = new byte[buffer.length * 2];
                        for (int i = 0; i < buffer.length; i++) {
                            doubleBuffer[i] = buffer[i];
                        }
                        buffer = doubleBuffer;
                    }
                } else {
                    byte[] result = new byte[totalRead];
                    for (int i = 0; i < totalRead; i++) {
                        result[i] = buffer[i];
                    }
                    return result;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

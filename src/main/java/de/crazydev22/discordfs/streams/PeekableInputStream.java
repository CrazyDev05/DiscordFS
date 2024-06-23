package de.crazydev22.discordfs.streams;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

@Data
@EqualsAndHashCode(callSuper = true)
public class PeekableInputStream extends InputStream {
    private final InputStream stream;
    private final Queue<@NonNull Integer> bytes = new ArrayDeque<>();

    @Override
    public int read() throws IOException {
        if (!bytes.isEmpty())
            return bytes.poll();
        return stream.read();
    }

    public int peek() throws IOException {
        int b = stream.read();
        bytes.add(b);
        return b;
    }

    public int peek(byte[] buffer) throws IOException {
        return peek(buffer, 0, buffer.length);
    }

    public int peek(byte[] buffer, int off, int len) throws IOException {
        Objects.checkFromIndexSize(off, len, buffer.length);
        if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        buffer[off] = (byte)c;

        int i = 1;
        try {
            for (; i < len ; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                buffer[off + i] = (byte)c;
            }
        } catch (IOException ignored) {}
        return i;
    }

    @Override
    public void close() throws IOException {
        try {
            stream.close();
        } finally {
            bytes.clear();
        }
    }
}

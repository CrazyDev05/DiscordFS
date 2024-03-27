package de.crazydev22.discordfs.streams;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
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
}

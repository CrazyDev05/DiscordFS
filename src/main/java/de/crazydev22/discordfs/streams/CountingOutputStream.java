package de.crazydev22.discordfs.streams;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Data
@EqualsAndHashCode(callSuper = true)
public class CountingOutputStream extends OutputStream {
	private final OutputStream stream;
	private long counter;

	@Override
	public void write(int b) throws IOException {
		stream.write(b);
		counter++;
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}
}

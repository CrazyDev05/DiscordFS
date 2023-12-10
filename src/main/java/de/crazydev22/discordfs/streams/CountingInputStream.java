package de.crazydev22.discordfs.streams;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.io.InputStream;

@Data
@EqualsAndHashCode(callSuper = true)
public class CountingInputStream extends InputStream {
	private final InputStream stream;
	private long counter;

	@Override
	public int read() throws IOException {
		int r = stream.read();
		if (r != -1)
			counter++;
		return r;
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}
}

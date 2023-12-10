package de.crazydev22.discordfs.streams;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.io.InputStream;

@Data
@EqualsAndHashCode(callSuper = true)
public class LimitingInputStream extends InputStream {
	private final InputStream inputStream;
	private long remainingBytes;

	@Override
	public int read() throws IOException {
		if (remainingBytes <= 0)
			return -1;
		remainingBytes--;
		return inputStream.read();
	}

	@Override
	public void close() throws IOException {
		inputStream.close();
	}
}

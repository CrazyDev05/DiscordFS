package de.crazydev22.discordfs.streams;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Supplier;

@Data
@EqualsAndHashCode(callSuper = true)
public class IteratingInputSteam extends InputStream {
	private final Iterator<InputStream> iterator;
	private InputStream stream = null;

	public IteratingInputSteam(Iterator<InputStream> iterator) {
		this.iterator = iterator;
	}

	public IteratingInputSteam(Supplier<Boolean> hasNext, Supplier<InputStream> next) {
		this(new Iterator<>() {
			@Override
			public boolean hasNext() {
				return hasNext.get();
			}

			@Override
			public InputStream next() {
				return next.get();
			}
		});
	}

	@Override
	public int read() throws IOException {
		if (stream == null) {
			if (!iterator.hasNext())
				return -1;
			stream = iterator.next();
		}
		int r = stream.read();
		if (r == -1 && iterator.hasNext()) {
			stream.close();
			stream = iterator.next();
			r = stream.read();
		}

		return r;
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}
}

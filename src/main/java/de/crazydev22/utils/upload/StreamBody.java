package de.crazydev22.utils.upload;

import de.crazydev22.discordfs.streams.CountingInputStream;
import de.crazydev22.discordfs.streams.CountingOutputStream;
import de.crazydev22.discordfs.streams.PeekableInputStream;
import de.crazydev22.utils.CipherUtil;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StreamBody {
	private static final byte[] CRLF = new byte[]{'\r', '\n'};

	@Getter
	private final List<byte[]> ivs = new ArrayList<>();
	@Getter
	private final List<Integer> sizes = new ArrayList<>();
	private final CountingInputStream source;
	private final PeekableInputStream peekable;
	private final @NonNull String boundary;
	private final byte[] key;
	private final int splitSize;
	private final int maxCount;
	@Getter
	private boolean done;

	public StreamBody(@NonNull PeekableInputStream source, @NonNull String boundary, byte[] key, int splitSize, int maxCount) {
		this.source = new CountingInputStream(source);
		this.peekable = source;
		this.boundary = URLEncoder.encode(boundary, StandardCharsets.UTF_8);
		this.key = key;
		this.splitSize = splitSize;
		this.maxCount = maxCount;
	}

	@NotNull
	public String contentType() {
		return "multipart/form-data; boundary="+boundary;
	}

	public void writeTo(@NotNull DataOutputStream dos) throws IOException {
		int count = 0;
		done = false;
		while (!done && count < maxCount) {
			dos.write(toBytes("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file"+count+"\"; filename=\"part-"+(count++)+".bin\""));
			dos.write(CRLF);
			dos.write(CRLF);
			done = writePart(dos);
			dos.write(CRLF);
		}
		dos.write(toBytes("--" + boundary + "--\r\n"));
	}

	private byte[] toBytes(String s) {
		return s.getBytes();
	}

	private boolean writePart(@NotNull DataOutputStream dos) {
		try {
			byte[] bytes = peekable.readNBytes(splitSize);
			if (bytes.length == 0) return true;
			if (key != null) {
				var pair = CipherUtil.encrypt(new ByteArrayInputStream(bytes), key);
				try (var in = pair.getB()) {
					in.transferTo(dos);
				}
				ivs.add(pair.getA());
			} else dos.write(bytes);
			sizes.add(bytes.length);
			return peekable.peek() == -1;
		} catch (Throwable e) {
			e.printStackTrace();
			return true;
		}
	}

	private boolean writeBytes(@NotNull DataOutputStream dos) {
		try {
			source.setCounter(0);
			byte[] buf = new byte[8192];
			var cos = new CountingOutputStream(dos);
			var out = getOutputStream(cos);
			while (source.getCounter() + buf.length <= splitSize && cos.getCounter() + buf.length <= splitSize) {
				int length = source.read(buf);
				if (length == -1) return true;
				out.write(buf, 0, length);
			}
			return false;
		} catch (Throwable e) {
			e.printStackTrace();
			return true;
		} finally {
			sizes.add((int) source.getCounter());
		}
	}

	private OutputStream getOutputStream(OutputStream out) {
		if (key == null) return out;
		var pair = CipherUtil.encrypt(out, key);
		ivs.add(pair.getA());
		return pair.getB();
	}
}

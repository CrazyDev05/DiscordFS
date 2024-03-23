package de.crazydev22.utils.upload;

import de.crazydev22.discordfs.streams.CountingInputStream;
import de.crazydev22.utils.CipherUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StreamBody extends RequestBody {
	private static final byte[] CRLF = new byte[]{'\r', '\n'};
	private static final byte[] DASHDASH = new byte[]{'-', '-'};

	@Getter
	private final List<byte[]> ivs = new ArrayList<>();
	@Getter
	private final List<Integer> sizes = new ArrayList<>();
	private final CountingInputStream source;
	private final @NonNull String boundary;
	private final byte[] key;
	private final long splitSize;
	private final int maxCount;
	@Getter
	private boolean done;

	public StreamBody(@NonNull InputStream source, @NonNull String boundary, @Nullable String key, long splitSize, int maxCount) {
		this.source = new CountingInputStream(source);
		this.boundary = URLEncoder.encode(boundary, StandardCharsets.UTF_8);
		this.key = key != null ? CipherUtil.createHash(key, 16) : null;
		this.splitSize = splitSize;
		this.maxCount = maxCount;
	}

	@Nullable
	@Override
	public MediaType contentType() {
		return MediaType.parse("multipart/form-data; boundary="+boundary);
	}

	@Override
	public void writeTo(@NotNull BufferedSink sink) throws IOException {
		int count = 0;
		done = false;
		while (!done && count < maxCount) {
			sink.write(DASHDASH).writeUtf8(boundary).write(CRLF);
			sink.writeUtf8("Content-Disposition: form-data; name=\"file"+count+"\"; filename=\"part-"+(count++)+".bin\"").write(CRLF);
			sink.write(CRLF);
			done = writeBytes(sink);
			sink.write(CRLF);
		}
		sink.write(DASHDASH).writeUtf8(boundary).write(DASHDASH).write(CRLF);
	}

	private boolean writeBytes(@NotNull BufferedSink sink) throws IOException {
		try {
			source.setCounter(0);
			byte[] buf = new byte[4096];
			var in = getInputStream();
			while (source.getCounter() + buf.length <= splitSize) {
				int length = in.read(buf);
				if (length == -1) return true;
				sink.write(buf, 0, length);
			}
			return false;
		} catch (Throwable e) {
			e.printStackTrace();
			return true;
		} finally {
			sizes.add((int) source.getCounter());
		}
	}

	private InputStream getInputStream() {
		if (key == null) return source;
		var pair = CipherUtil.encrypt(source, key);
		ivs.add(pair.getA());
		return pair.getB();
	}
}

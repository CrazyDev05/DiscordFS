package de.crazydev22.utils.upload;

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

@RequiredArgsConstructor
public class StreamBody extends RequestBody {
	private static final byte[] CRLF = new byte[]{'\r', '\n'};
	private static final byte[] DASHDASH = new byte[]{'-', '-'};

	@Getter
	private final List<byte[]> ivs = new ArrayList<>();
	@Getter
	private final List<Integer> sizes = new ArrayList<>();
	private final @NonNull InputStream source;
	private final @NonNull String boundary;
	private final byte[] key;
	private final long splitSize;
	private final int maxCount;
	@Getter
	private boolean done;

	public StreamBody(@NonNull InputStream source, @NonNull String boundary, @Nullable String key, long splitSize, int maxCount) {
		this(source, URLEncoder.encode(boundary, StandardCharsets.UTF_8), key != null ? CipherUtil.createHash(key, 16) : null, splitSize, maxCount);
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
		byte[] buf = new byte[8096];
		while (!done && count < maxCount) {
			sink.write(DASHDASH).writeUtf8(boundary).write(CRLF);
			sink.writeUtf8("Content-Disposition: form-data; name=\"file"+count+"\"; filename=\"part-"+(count++)+".bin\"").write(CRLF);
			sink.write(CRLF);
			done = writeBytes(sink, buf);
			sink.write(CRLF);
		}
		sink.write(DASHDASH).writeUtf8(boundary).write(DASHDASH).write(CRLF);
	}

	private boolean writeBytes(@NotNull BufferedSink sink, byte[] buf) throws IOException {
		int bytes = 0;
		var out = getOutputStream(sink);
		try {
			while (bytes + buf.length <= splitSize) {
				int length = source.read(buf);
				if (length == -1) return true;
				out.write(buf, 0, length);
				bytes += length;
			}
			return false;
		} finally {
			sizes.add(bytes);
		}
	}

	private OutputStream getOutputStream(BufferedSink sink) {
		OutputStream out = sink.outputStream();
		if (key == null) return out;
		var pair = CipherUtil.encrypt(sink.outputStream(), key);
		ivs.add(pair.getA());
		return pair.getB();
	}
}

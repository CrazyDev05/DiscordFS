package de.crazydev22.discordfs;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.receive.ReadonlyAttachment;
import club.minnced.discord.webhook.receive.ReadonlyMessage;
import de.crazydev22.discordfs.streams.DiscordFileInputStream;
import de.crazydev22.discordfs.streams.LimitingInputStream;
import de.crazydev22.utils.CipherUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data
@AllArgsConstructor
public final class DiscordFile {
	private static final String OCTET_STREAM = "application/octet-stream";

	private final @NotNull String id;
	private final @NotNull String token;
	private final @NotNull String name;
	private final @NotNull String mime;
	private final @NotNull List<Part> parts;

	private DiscordFile(@NotNull String id, @NotNull String name, @NotNull String mime) {
		this(id, RandomStringUtils.randomAlphanumeric(20), name, mime, new ArrayList<>());
	}

	@NotNull
	public static DiscordFile create(@NotNull String name, @Nullable String mime) {
		return create(RandomStringUtils.randomAlphanumeric(10), name, mime);
	}

	@NotNull
	public static DiscordFile create(@NotNull String id, @NotNull String name, @Nullable String mime) {
		if (mime == null) mime = OCTET_STREAM;
		return new DiscordFile(id, name, mime);
	}

	public long getSize() {
		long size = 0;
		for (var part : parts)
			size+= part.getSize();
		return size;
	}

	public boolean notMatchesToken(@Nullable String token) {
		return token == null || token.length() <= 7 || !getToken().equals(token.substring(7));
	}

	@SneakyThrows
	public InputStream read(WebhookClient client, String key, long start, long end) {
		var stream = read(client, key);
		var res = findPartURL(start);
		stream.setNextPart(res[0]);
		stream.setNextURL(res[1]);
		stream.skipNBytes(res[2]);


		var limit = new LimitingInputStream(stream);
		limit.setRemainingBytes(end-start);
		return limit;
	}

	private int[] findPartURL(long b) {
		int p = 0;
		int u = 0;
		int s = 0;
		while (b > 0) {
			var part = parts.get(p++);
			u=0;
			for (int size : part.partSizes) {
				b -= size;
				if (b <= 0) {
					s = (int) (size+b);
					break;
				}
				u++;
			}
		}
		return new int[]{p == 0 ? p : p-1, u, s};
	}

	public DiscordFileInputStream read(WebhookClient client, String key) {
		return new DiscordFileInputStream(this, client, CipherUtil.createHash(key, 16));
	}

	public record Part(long messageID, List<byte[]> ivs, List<Integer> partSizes) implements Cloneable {
		public long getSize() {
			long size = 0;
			for (int part : partSizes)
				size+=part;
			return size;
		}

		public CompletableFuture<List<String>> loadAttachments(WebhookClient client) {
			return client.get(messageID)
					.thenApply(ReadonlyMessage::getAttachments)
					.thenApply(Part::getURLs);
		}

		@Override
		@SuppressWarnings("all")
		public Part clone() {
			Part clone = new Part(messageID, new ArrayList<>(), new ArrayList<>());
			for (byte[] iv : ivs)
				clone.ivs.add(Arrays.copyOf(iv, iv.length));
			for (int size : partSizes)
				clone.partSizes.add(size);
			return clone;
		}

		private static List<String> getURLs(List<ReadonlyAttachment> attachments) {
			return attachments.stream()
					.sorted(Comparator.comparing(ReadonlyAttachment::getFileName))
					.map(ReadonlyAttachment::getUrl)
					.toList();
		}
	}
}

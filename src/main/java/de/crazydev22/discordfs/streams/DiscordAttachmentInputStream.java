package de.crazydev22.discordfs.streams;

import club.minnced.discord.webhook.WebhookClient;
import de.crazydev22.discordfs.DiscordFile;
import de.crazydev22.utils.CipherUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiscordAttachmentInputStream extends InputStream {
	private final IteratingInputSteam inputSteam = new IteratingInputSteam(this::hasNext, this::next);
	private final DiscordFile.Part part;
	private final List<String> urls;
	private final String key;
	private int nextURL = 0;

	@SneakyThrows
	public DiscordAttachmentInputStream(DiscordFile.Part part, WebhookClient client, String key) {
		this.part = part;
		this.urls = part.loadAttachments(client).get();
		this.key = key;
	}

	@Override
	public int read() throws IOException {
		return inputSteam.read();
	}

	@Override
	public void close() throws IOException {
		inputSteam.close();
	}

	@SneakyThrows
	private boolean hasNext() {
		return nextURL < urls.size();
	}

	@SneakyThrows
	private InputStream next() {
		int i = nextURL++;
		return CipherUtil.decrypt(new URL(urls.get(i)).openStream(), key, part.ivs().get(i));
	}
}

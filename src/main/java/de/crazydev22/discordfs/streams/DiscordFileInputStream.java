package de.crazydev22.discordfs.streams;

import club.minnced.discord.webhook.WebhookClient;
import de.crazydev22.discordfs.DiscordFile;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.io.InputStream;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiscordFileInputStream extends InputStream {
	private final IteratingInputSteam inputSteam = new IteratingInputSteam(this::hasNext, this::next);
	private final DiscordFile file;
	private final WebhookClient client;
	private final byte[] key;
	private int nextPart = 0;
	private int nextURL = -1;

	@Override
	public int read() throws IOException {
		return inputSteam.read();
	}

	@Override
	public void close() throws IOException {
		inputSteam.close();
	}

	private boolean hasNext() {
		return nextPart < file.getParts().size();
	}

	private InputStream next() {
		var stream = new DiscordAttachmentInputStream(file.getParts().get(nextPart++), client, key);
		if (nextURL > -1) {
			stream.setNextURL(nextURL);
			nextURL-=stream.getUrls().size();
		}
		return stream;
	}
}

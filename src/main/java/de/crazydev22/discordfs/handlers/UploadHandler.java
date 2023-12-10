package de.crazydev22.discordfs.handlers;

import club.minnced.discord.webhook.send.WebhookMessage;
import com.sun.net.httpserver.HttpExchange;
import de.crazydev22.discordfs.DiscordFile;
import de.crazydev22.utils.CipherUtil;
import de.crazydev22.utils.IHandler;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UploadHandler extends IIHandler {
	private final String key = getConfig().getString("cipher").orElseThrow();
	private static int maxFileSize = 1024*1024*10; //10MiB needs to be devidable by 512
	private static int attachmetsPerFile = 2;

	public UploadHandler() {
		super(LoggerFactory.getLogger(UploadHandler.class));
		setDebug(true);
	}

	@Override
	public void PUT(@NotNull HttpExchange exchange) throws Throwable{
		String name = exchange.getRequestURI().getPath().substring(1);
		var file = uploadFile(name, exchange.getRequestBody());
		getDatabase().saveFile(file);
		exchange.getResponseHeaders()
				.set("Content-Type", "application/json; charset=utf-8");
		sendText(exchange, 200, "{\"token\": \"" + file.getToken() + "\", \"id\":\"" + file.getId() + "\"}");
	}

	@SneakyThrows
	private DiscordFile uploadFile(String name, InputStream inputStream) {
		var done = false;
		var file = DiscordFile.create(name);

		try (var stream = inputStream) {
			while (!done) {
				Map<String, byte[]> cache = new TreeMap<>(String::compareTo);
				List<Integer> sizes = new ArrayList<>(attachmetsPerFile);
				List<byte[]> ivs = new ArrayList<>(attachmetsPerFile);

				for (int i = 0; i < attachmetsPerFile; i++) {
					var bytes = stream.readNBytes(maxFileSize);
					var pair = CipherUtil.encrypt(bytes, key);
					cache.put("part-"+i+".bin", pair.getB());
					ivs.add(pair.getA());
					sizes.add(bytes.length);
					if (bytes.length != maxFileSize) {
						done = true;
						break;
					}
				}

				file.getParts().add(new DiscordFile.Part(getClient().send(WebhookMessage.files(cache)).get().getId(), ivs, sizes));
			}
		}
		return file;
	}
}

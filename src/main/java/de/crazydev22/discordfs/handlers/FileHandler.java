package de.crazydev22.discordfs.handlers;

import club.minnced.discord.webhook.send.WebhookMessage;
import de.crazydev22.discordfs.DiscordFile;
import de.crazydev22.utils.CipherUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FileHandler extends IIHandler {
	private final String key = getConfig().getString("cipher").orElseThrow();

	private static int maxFileSize = 1024*1024*5; //10MiB needs to be devidable by 512
	private static int attachmetsPerFile = 4;

	@Override
	public void GET(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		getLogger().info(request.toString());

		DiscordFile file = getDatabase().getFile(request.getRequestURI().substring("/files/".length()));
		if (file == null) {
			sendText(response, 404, "File not found");
			return;
		}


		if (file.getParts().isEmpty()) {
			sendText(response, 503, "File empty");
			return;
		}

		var rangeHeader = request.getHeader("Range");
		if (rangeHeader != null) {
			var rangeValue = rangeHeader.trim().substring("bytes=".length());
			long fileLength = file.getSize();
			long start, end;
			if (rangeValue.startsWith("-")) {
				end = fileLength;
				start = fileLength - Long.parseLong(rangeValue.substring("-".length()));
			} else {
				String[] range = rangeValue.split("-");
				start = Long.parseLong(range[0]);
				end = range.length > 1 ? Long.parseLong(range[1]) : fileLength;
			}
			if (end > fileLength) {
				end = fileLength;
			}
			if (start <= end) {
				long contentLength = end - start;
				response.setHeader("Content-Range", "bytes " + start + "-"
						+ end + "/" + fileLength);
				response.setHeader("Access-Control-Allow-Origin", "*");
				response.setHeader("Content-Length", contentLength + "");
				response.setHeader("Content-Type", file.getMime());
				transferStreamAsync(request, response, file.read(getClient(), key, start, end), 206, 8192);
				return;
			}
		}

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Content-Length", file.getSize() + "");
		response.setHeader("Content-Type", file.getMime());
		transferStreamAsync(request, response, file.read(getClient(), key), 200, 8192);
	}

	@Override
	public void PUT(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		String name = request.getRequestURI().substring("/files/".length());
		var file = uploadFile(name, request.getInputStream());
		getDatabase().saveFile(file);
		response.setHeader("Content-Type", "application/json; charset=utf-8");
		sendText(response, 200, "{\"token\": \"" + file.getToken() + "\", \"id\":\"" + file.getId() + "\"}");
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

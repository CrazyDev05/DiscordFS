package de.crazydev22.discordfs.handlers;

import club.minnced.discord.webhook.send.WebhookMessage;

import de.crazydev22.discordfs.DiscordFile;
import de.crazydev22.utils.CipherUtil;
import de.crazydev22.utils.upload.StreamBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileHandler extends IIHandler {
	private final String key = getConfig().getString("cipher").orElseThrow();

	private static int maxFileSize = 1024*1024*5; //10MiB needs to be devidable by 512
	private static int attachmetsPerFile = 4;

	@Override
	public void GET(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		DiscordFile file = getFile(request.getRequestURI());
		if (file == null) {
			sendText(response, 404, "File not found");
			return;
		}

		if (file.getParts().isEmpty()) {
			sendText(response, 503, "File empty");
			return;
		}

		var rangeHeader = request.getHeader("Range");
		if (rangeHeader != null && getConfig().getBoolean("rangeHeader").orElse(true)) {
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
		name = URLDecoder.decode(name, StandardCharsets.UTF_8);
		while (name.contains("\\")) name = name.replace("\\", "/");
		while (name.contains("//")) name = name.replace("//", "/");

		var file = uploadFile(request.getHeader("file-id"), name, request.getInputStream());
		getDatabase().saveFile(file);
		response.setHeader("Content-Type", "application/json; charset=utf-8");
		JSONObject object = new JSONObject();
		object.put("token", file.getToken());
		object.put("id", file.getId());
		object.put("url", "/files/" + file.getId() + "/" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));
		sendText(response, 200, object.toString());
	}

	@Override
	public void DELETE(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		DiscordFile file = getFile(request.getRequestURI());
		if (file == null || !file.matchesToken(request.getHeader("Authorization"))) {
			sendText(response, 404, "File not found");
			return;
		}

		for (var part : file.getParts())
			getClient().delete(part.messageID());

		getDatabase().deleteFile(file.getId(), file.getName(), file.getToken());
	}

	private DiscordFile getFile(String uri) {
		String[] parts = uri.substring("/files/".length()).split("/", 2);
		for (int i = 0; i < parts.length; i++)
			parts[i] = URLDecoder.decode(parts[i], StandardCharsets.UTF_8);
		return getDatabase().getFile(parts[0], parts[1]);
	}

	@SneakyThrows
	private DiscordFile uploadFile(@Nullable String id, @NotNull String name, @NotNull InputStream inputStream) {
		var done = false;
		var file = id != null ?
				DiscordFile.create(id, name, URLConnection.guessContentTypeFromName(name)) :
				DiscordFile.create(name, URLConnection.guessContentTypeFromName(name));

		try (var stream = inputStream) {
			var key = CipherUtil.createHash(this.key, 16);
			while (!done) {
				var body = new StreamBody(stream, UUID.randomUUID().toString(), key, maxFileSize, attachmetsPerFile);
				file.getParts().add(new DiscordFile.Part(getClient().send(body).get().getId(), body.getIvs(), body.getSizes()));
				done = body.isDone();
			}
		}
		return file;
	}
}

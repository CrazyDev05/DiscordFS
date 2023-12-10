package de.crazydev22.discordfs.handlers;

import com.sun.net.httpserver.HttpExchange;
import de.crazydev22.discordfs.DiscordFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

public class DownloadHandler extends IIHandler {
	private final String key = getConfig().getString("cipher").orElseThrow();

	public DownloadHandler() {
		super(LoggerFactory.getLogger(DownloadHandler.class));
		setDebug(true);
	}

	@Override
	public void GET(@NotNull HttpExchange exchange) throws Throwable {
		DiscordFile file = getDatabase().getFile(exchange.getRequestURI().getPath().substring("/download/".length()));
		if (file == null) {
			sendText(exchange, 404, "File not found");
			return;
		}


		if (file.getParts().isEmpty()) {
			sendText(exchange, 503, "File empty");
			return;
		}
		var reqHeaders = exchange.getRequestHeaders();
		var headers = exchange.getResponseHeaders();

		if (reqHeaders.containsKey("Range")) {
			var rangeValue = reqHeaders.getFirst("Range")
					.trim()
					.substring("bytes=".length());
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
				exchange.sendResponseHeaders(206, contentLength);
				headers.set("Content-Range", "bytes " + start + "-"
						+ end + "/" + fileLength);
				headers.set("Access-Control-Allow-Origin", "*");
				headers.set("Content-Length", contentLength + "");
				headers.set("Content-Type", file.getMime());
				transferStream(file.read(getClient(), key, start, end), exchange.getResponseBody(), 8192);
				exchange.close();
				return;
			}
		}

		exchange.sendResponseHeaders(200, file.getSize());
		headers.set("Access-Control-Allow-Origin", "*");
		headers.set("Content-Length", file.getSize() + "");
		headers.set("Content-Type", file.getMime());
		transferStream(file.read(getClient(), key), exchange.getResponseBody(), 8192);
		exchange.close();
	}
}

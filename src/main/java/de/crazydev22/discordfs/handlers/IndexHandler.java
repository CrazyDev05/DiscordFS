package de.crazydev22.discordfs.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

public class IndexHandler extends IIHandler {
	private final File ROOT = new File("data");
	private final int ROOT_LENGTH = ROOT.getAbsolutePath().length();

	public IndexHandler() {
		super(LoggerFactory.getLogger(IndexHandler.class));
		setDebug(true);
	}

	@Override
	protected void GET(@NotNull HttpExchange exchange) throws Throwable {
		String path = exchange.getRequestURI().getPath().substring(1);
		if (path.isEmpty())
			path = "index.html";
		File file = new File(ROOT, path);
		if (file.getAbsolutePath().length() < ROOT_LENGTH || !file.isFile()) {
			sendText(exchange, 404, "");
			return;
		}

		exchange.sendResponseHeaders(200, file.length());
		try (var out = exchange.getResponseBody(); var in = new FileInputStream(file)) {
			transferStream(in, out, 8192);
		}
		exchange.close();
	}
}

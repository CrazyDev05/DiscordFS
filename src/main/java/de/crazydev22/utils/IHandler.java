package de.crazydev22.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

@Data
public abstract class IHandler implements HttpHandler {
	private static final Map<Class<? extends Throwable>, List<String>> IGNORED = Map.of(
			IOException.class, List.of("Connection reset by peer")
	);

	private final Logger logger;
	private boolean debug;

	public IHandler() {
		this.logger = LoggerFactory.getLogger(IHandler.class);
	}

	public IHandler(Logger logger) {
		this.logger = logger;
	}

	public void handle(HttpExchange exchange) throws IOException {
		try {
			switch (exchange.getRequestMethod().toUpperCase()) {
				case "GET" -> GET(exchange);
				case "HEAD" -> HEAD(exchange);
				case "POST" -> POST(exchange);
				case "PUT" -> PUT(exchange);
				case "DELETE" -> DELETE(exchange);
				case "CONNECT" -> CONNECT(exchange);
				case "OPTIONS" -> OPTIONS(exchange);
				case "TRACE" -> TRACE(exchange);
				case "PATCH" -> PATCH(exchange);
			}
		} catch (Throwable e) {
			if (IGNORED.containsKey(e.getClass()) && IGNORED.get(e.getClass()).contains(e.getMessage()))
				return;

			logger.error(exchange.getRemoteAddress() + " " + exchange.getRequestMethod() + " " + exchange.getRequestURI(), e);
			if (debug) {
				StringWriter writer = new StringWriter();
				e.printStackTrace(new PrintWriter(writer));
				byte[] body = writer.toString().getBytes();
				exchange.sendResponseHeaders(500, body.length);
				try (var out = exchange.getResponseBody()) {
					out.write(body);
				}
			} else {
				exchange.sendResponseHeaders(500, 0);
			}
			exchange.close();
		}
	}

	protected void GET(@NotNull HttpExchange exchange) throws Throwable {
		exchange.sendResponseHeaders(501, 0);
		exchange.close();
	}

	protected void HEAD(@NotNull HttpExchange exchange) throws Throwable {
		exchange.sendResponseHeaders(501, 0);
		exchange.close();
	}

	protected void POST(@NotNull HttpExchange exchange) throws Throwable {
		exchange.sendResponseHeaders(501, 0);
		exchange.close();
	}

	protected void PUT(@NotNull HttpExchange exchange) throws Throwable {
		exchange.sendResponseHeaders(501, 0);
		exchange.close();
	}

	protected void DELETE(@NotNull HttpExchange exchange) throws Throwable {
		exchange.sendResponseHeaders(501, 0);
		exchange.close();
	}

	protected void CONNECT(@NotNull HttpExchange exchange) throws Throwable {
		exchange.sendResponseHeaders(501, 0);
		exchange.close();
	}

	protected void OPTIONS(@NotNull HttpExchange exchange) throws Throwable {
		exchange.sendResponseHeaders(501, 0);
		exchange.close();
	}

	protected void TRACE(@NotNull HttpExchange exchange) throws Throwable {
		exchange.sendResponseHeaders(501, 0);
		exchange.close();
	}

	protected void PATCH(@NotNull HttpExchange exchange) throws Throwable {
		exchange.sendResponseHeaders(501, 0);
		exchange.close();
	}
}

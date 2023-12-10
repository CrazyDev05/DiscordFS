package de.crazydev22.discordfs.handlers;

import club.minnced.discord.webhook.WebhookClient;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import de.crazydev22.discordfs.Database;
import de.crazydev22.discordfs.Main;
import de.crazydev22.utils.IHandler;
import de.crazydev22.utils.JsonConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Data
@EqualsAndHashCode(callSuper = true)
public class IIHandler extends IHandler {
	private final Database database = Main.getInstance().getDatabase();
	private final WebhookClient client = Main.getInstance().getWebhook();
	private final JsonConfiguration<JsonObject> config = Main.getInstance().getConfiguration();

	public IIHandler(Logger logger) {
		super(logger);
	}

	protected void transferStream(InputStream in, OutputStream out, int buffer) throws IOException {
		byte[] buf = new byte[buffer];
		int length;
		try (in; out) {
			while ((length = in.read(buf)) != -1) {
				out.write(buf, 0, length);
			}
		}
	}

	protected void sendText(HttpExchange exchange, int code, String msg) throws IOException {
		exchange.sendResponseHeaders(code, msg.getBytes().length);
		try (var out = exchange.getResponseBody()) {
			out.write(msg.getBytes());
		}
		exchange.close();
	}
}

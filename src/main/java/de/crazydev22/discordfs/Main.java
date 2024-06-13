package de.crazydev22.discordfs;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import com.google.gson.JsonObject;
import de.crazydev22.discordfs.handlers.FileHandler;
import de.crazydev22.discordfs.handlers.IndexHandler;
import de.crazydev22.discordfs.handlers.RoutingHandler;
import de.crazydev22.utils.CipherUtil;
import de.crazydev22.utils.JsonConfiguration;
import lombok.Data;
import lombok.Getter;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Data
public class Main {
	public static final Logger logger = LoggerFactory.getLogger("DiscordFS");

	private static @Getter Main instance;

	private final JsonConfiguration<JsonObject> configuration = new JsonConfiguration<>("config.json", JsonConfiguration.fromMap(
			"storage", "files",
			"cipher", "",
			"webhook", "https://discord.com/api/webhooks/<>/<>",
			"web.minThreads", 1,
			"web.maxThreads", Runtime.getRuntime().availableProcessors(),
			"web.idleTimeout", 120,
			"web.rangeHeader", true,
			"web.port", 8080
	).getContent());
	private final ThreadPool threadPool;
	private final WebhookClient webhook;
	private final Database database;
	private final Server server;
	private final byte[] cipher;

	public Main() throws Exception {
		instance = this;

		threadPool = new QueuedThreadPool(
				configuration.getInt("web.maxThreads").orElse(Runtime.getRuntime().availableProcessors()),
				configuration.getInt("web.minThreads").orElse(1),
				configuration.getInt("web.idleTimeout").orElse(120));
		database = new Database(new File(configuration.getString("storage").orElse("files")));
		cipher = configuration.getString("cipher").map(key -> CipherUtil.createHash(key, 16)).orElseThrow();

		var builder = new WebhookClientBuilder(configuration.getString("webhook").orElseThrow());
		builder.setWait(true);
		webhook = builder.build();

		server = new Server(threadPool);
		var connector = new ServerConnector(server);
		connector.setPort(configuration.getInt("web.port").orElse(8080));
		server.setConnectors(new Connector[]{connector});

		var servletHandler = new ServletHandler();
		server.setHandler(servletHandler);
		RoutingHandler handler = new RoutingHandler();
		var routes = handler.getRoutes();
		routes.add(0, "^/files/.*", new FileHandler());
		routes.add(Integer.MAX_VALUE, ".*", new IndexHandler());

		servletHandler.addServletWithMapping(new ServletHolder(handler), "/*");

		server.start();
	}

	public static void main(String[] args) throws Exception {
		new Main();
	}
}
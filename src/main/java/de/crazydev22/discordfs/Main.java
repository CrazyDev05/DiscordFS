package de.crazydev22.discordfs;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.crazydev22.discordfs.handlers.FileHandler;
import de.crazydev22.discordfs.handlers.IndexHandler;
import de.crazydev22.discordfs.handlers.RoutingHandler;
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

import java.util.List;
import java.util.Map;

@Data
public class Main {
	public static final Logger logger = LoggerFactory.getLogger("DiscordFS");
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(DiscordFile.Part.class, new DiscordFile.Part.Adapter())
			.create();

	private static @Getter Main instance;

	private final JsonConfiguration<JsonObject> configuration = new JsonConfiguration<>("config.json", JsonConfiguration.fromMap(
			"mariadb.host", "localhost",
			"mariadb.port", 3306,
			"mariadb.database", "discordfs",
			"mariadb.user", "discordfs",
			"mariadb.password", "password",
			"cipher", "",
			"webhook", "https://discord.com/api/webhooks/<>/<>",
			"minThreads", 1,
			"maxThreads", Runtime.getRuntime().availableProcessors(),
			"idleTimeout", 120,
			"rangeHeader", true
	).getContent());
	private final ThreadPool threadPool;
	private final WebhookClient webhook;
	private final Database database;
	private final Server server;

	public Main() throws Exception {
		instance = this;

		threadPool = new QueuedThreadPool(
				configuration.getInt("maxThreads").orElse(Runtime.getRuntime().availableProcessors()),
				configuration.getInt("minThreads").orElse(1),
				configuration.getInt("idleTimeout").orElse(120));
		database = new Database(
				configuration.getString("mariadb.host").orElseThrow(),
				configuration.getInt("mariadb.port").orElseThrow(),
				configuration.getString("mariadb.database").orElseThrow(),
				configuration.getString("mariadb.user").orElseThrow(),
				configuration.getString("mariadb.password").orElseThrow());
		WebhookClientBuilder builder = new WebhookClientBuilder(configuration.getString("webhook").orElseThrow());
		builder.setWait(true);
		webhook = builder.build();

		server = new Server(threadPool);
		var connector = new ServerConnector(server);
		connector.setPort(8080);
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
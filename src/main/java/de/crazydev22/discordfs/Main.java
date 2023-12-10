package de.crazydev22.discordfs;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import de.crazydev22.discordfs.handlers.DownloadHandler;
import de.crazydev22.discordfs.handlers.IndexHandler;
import de.crazydev22.discordfs.handlers.UploadHandler;
import de.crazydev22.utils.JsonConfiguration;
import de.crazydev22.utils.JsonUtil;
import lombok.Data;
import lombok.Getter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Data
public class Main {
	public static final Logger logger = LoggerFactory.getLogger("DiscordFS");
	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapter(DiscordFile.Part.class, new DiscordFile.Part.Adapter())
			.create();

	private static @Getter Main instance;

	private final JsonConfiguration<JsonObject> configuration = new JsonConfiguration<>("config.json", JsonConfiguration.fromMap(Map.of(
			"mariadb.host", "localhost",
			"mariadb.port", 3306,
			"mariadb.database", "discordfs",
			"mariadb.user", "discordfs",
			"mariadb.password", "password",
			"cipher", "",
			"webhook", "https://discord.com/api/webhooks/<>/<>",
			"threads", 10
	)).getContent());
	private final Executor executor;
	private final WebhookClient webhook;
	private final Database database;
	private final HttpServer server;

	public Main() throws Exception {
		instance = this;

		executor = Executors.newFixedThreadPool(configuration.getInt("threads").orElseThrow());
		database = new Database(
				configuration.getString("mariadb.host").orElseThrow(),
				configuration.getInt("mariadb.port").orElseThrow(),
				configuration.getString("mariadb.database").orElseThrow(),
				configuration.getString("mariadb.user").orElseThrow(),
				configuration.getString("mariadb.password").orElseThrow());
		WebhookClientBuilder builder = new WebhookClientBuilder(configuration.getString("webhook").orElseThrow());
		builder.setWait(true);
		webhook = builder.build();

		server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/download", new DownloadHandler());
		server.createContext("/upload", new UploadHandler());
		server.createContext("/", new IndexHandler());
		server.setExecutor(executor);
		server.start();

		logger.info("Started!");
	}

	public static void main(String[] args) throws Exception {
		new Main();
	}
}
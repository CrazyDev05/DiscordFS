package de.crazydev22.discordfs;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import com.google.gson.JsonObject;
import de.crazydev22.utils.JsonConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class Main {
	private static Main instance;

	private final JsonConfiguration<JsonObject> configuration = new JsonConfiguration<>("config.json", JsonConfiguration.fromMap(Map.of(
			"mariadb.host", "localhost",
			"mariadb.port", 3306,
			"mariadb.database", "discordfs",
			"mariadb.user", "discordfs",
			"mariadb.password", "password",
			"cipher", "",
			"webhook", "https://discord.com/api/webhooks/<>/<>"
	)).getContent());
	private final WebhookClient webhook;
	private final Database database;
	private final Server server;

	public Main() throws Exception {
		instance = this;

		database = new Database(
				configuration.getString("mariadb.host").orElseThrow(),
				configuration.getInt("mariadb.port").orElseThrow(),
				configuration.getString("mariadb.database").orElseThrow(),
				configuration.getString("mariadb.user").orElseThrow(),
				configuration.getString("mariadb.password").orElseThrow());
		WebhookClientBuilder builder = new WebhookClientBuilder(configuration.getString("webhook").orElseThrow());
		builder.setWait(true);
		webhook = builder.build();

		server = new Server(8000);
		var handler = new ServletHandler();
		handler.addServletWithMapping(Servlet.class, "/*");
		server.setHandler(handler);
		server.start();
	}

	public static void main(String[] args) throws Exception {
		new Main();
	}

	public Database getDatabase() {
		return database;
	}

	public WebhookClient getWebhook() {
		return webhook;
	}



	public void stop() throws Exception {
		server.stop();
	}

	public static Main getInstance() {
		return instance;
	}
}
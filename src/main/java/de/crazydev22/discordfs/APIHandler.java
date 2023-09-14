package de.crazydev22.discordfs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class APIHandler implements HttpHandler {
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		JsonObject obj = new JsonObject();
		obj.addProperty("msg", "Hello World!");
		obj.addProperty("uri", exchange.getRequestURI().toString());

		String content = gson.toJson(obj);
		exchange.sendResponseHeaders(200, content.length());
		OutputStream os = exchange.getResponseBody();
		os.write(content.getBytes());
		os.close();
	}
}

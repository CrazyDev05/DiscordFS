package de.crazydev22.discordfs;

import com.sun.net.httpserver.HttpServer;
import de.crazydev22.discordfs.APIHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/api", new APIHandler());
		server.setExecutor(null);
		server.start();
	}
}
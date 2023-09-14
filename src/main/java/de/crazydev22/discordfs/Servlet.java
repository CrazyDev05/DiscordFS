package de.crazydev22.discordfs;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.stream.Collectors;

public class Servlet extends HttpServlet {
	private final Database database = Main.getInstance().getDatabase();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		var file = database.getFile(request.getRequestURI());
		if (file == null) {
			sendText(response, 404, "File not found");
			return;
		}

		if (!file.file()) {
			var msg = file.listFiles(database).stream()
					.map(sub -> "<a href="+sub.name()+">"+sub.name()+(sub.file() ? "" : "/")+"</a>")
					.collect(Collectors.joining("<br>"));
			sendText(response, 200, msg);
		} else {
			if (!file.data().has("urls")) {
				sendText(response, 503, "");
				return;
			}
			var urls = file.data().getAsJsonArray("urls");
			response.setContentType("application/octet-stream");
			try (OutputStream out = response.getOutputStream()) {
				for (int i = 0; i < urls.size(); i++) {
					//TODO decrypt the input stream
					try (InputStream in = new URL(urls.get(i).getAsString()).openStream()) {
						transferStream(in, out, 8192);
					}
				}
			}
		}
	}

	private void transferStream(InputStream in, OutputStream out, int buffer) throws IOException {
		byte[] buf = new byte[buffer];
		int length;
		while ((length = in.read(buf)) != -1) {
			out.write(buf, 0, length);
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		sendText(response, 200, request.getMethod());
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		sendText(response, 200, request.getMethod());
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		sendText(response, 200, request.getMethod());
	}


	private void sendText(HttpServletResponse response, int code, String msg) throws IOException {
		response.setStatus(code);
		response.getWriter().println(msg);
	}
}

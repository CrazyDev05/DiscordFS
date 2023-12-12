package de.crazydev22.discordfs.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;

public class IndexHandler extends IIHandler {
	private final File ROOT = new File("data");
	private final int ROOT_LENGTH = ROOT.getAbsolutePath().length();

	@Override
	public void GET(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		String path = request.getRequestURI().substring(1);
		if (path.isEmpty())
			path = "index.html";
		File file = new File(ROOT, path);
		if (file.getAbsolutePath().length() < ROOT_LENGTH || !file.isFile()) {
			sendText(response, 404, "Not Found");
			return;
		}

		response.setHeader("Content-Length", file.length() + "");
		transferStreamAsync(request, response, new FileInputStream(file), 200, 8192);
	}
}

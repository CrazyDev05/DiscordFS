package de.crazydev22.discordfs.handlers;

import club.minnced.discord.webhook.WebhookClient;
import com.google.gson.JsonObject;
import de.crazydev22.discordfs.Database;
import de.crazydev22.discordfs.Main;
import de.crazydev22.discordfs.streams.CountingOutputStream;
import de.crazydev22.utils.IHandler;
import de.crazydev22.utils.JsonConfiguration;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.HttpChannelState;
import org.eclipse.jetty.server.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class IIHandler extends IHandler {
	private final Database database = Main.getInstance().getDatabase();
	private final WebhookClient client = Main.getInstance().getWebhook();
	private final JsonConfiguration<JsonObject> config = Main.getInstance().getConfiguration();

	@Override
	public void GET(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.GET(request, response);
	}

	@Override
	public void HEAD(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.HEAD(request, response);
	}

	@Override
	public void POST(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.POST(request, response);
	}

	@Override
	public void PUT(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.PUT(request, response);
	}

	@Override
	public void DELETE(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.DELETE(request, response);
	}

	@Override
	public void OPTIONS(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.OPTIONS(request, response);
	}

	@Override
	public void TRACE(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Throwable {
		super.TRACE(request, response);
	}

	protected final void transferStreamAsync(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
											 @NotNull InputStream in, int status, int buffer) throws IOException {
		var async = request.startAsync();
		async.setTimeout(3*3600000);
		var out = response.getOutputStream();
		out.setWriteListener(new WriteListener() {
			private final byte[] buf = new byte[buffer];

			@Override
			public void onWritePossible() throws IOException {
				while (out.isReady()) {
					int length = in.read(buf);
					if (length == -1) {
						response.setStatus(status);
						async.complete();
						return;
					}
					out.write(buf, 0, length);
				}
			}

			@Override
			public void onError(Throwable t) {
				getServletContext().log("Async Error", t);
				async.complete();
			}
		});
	}

	protected final void sendText(@NotNull HttpServletResponse response, int code, @NotNull String msg) throws IOException {
		response.setStatus(code);
		response.getWriter().println(msg);
	}
}

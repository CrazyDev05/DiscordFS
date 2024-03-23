package de.crazydev22.utils.upload;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.receive.ReadonlyMessage;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.util.ThreadPools;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

public class Webhook extends WebhookClient{

	private Webhook(long id, String token, boolean parseMessage, OkHttpClient client, ScheduledExecutorService pool, AllowedMentions mentions, long threadId) {
		super(id, token, parseMessage, client, pool, mentions, threadId);
	}

	public @NotNull CompletableFuture<ReadonlyMessage> send(@NotNull StreamBody body) {
		return execute(body);
	}

	@Override
	public @NotNull CompletableFuture<ReadonlyMessage> execute(RequestBody body) {
		return super.execute(body);
	}

	public static class Builder extends WebhookClientBuilder {
		public Builder(@NotNull String url) {
			super(url);
		}

		public @NotNull Webhook buildWebhook() {
			OkHttpClient client = this.client == null ? new OkHttpClient() : this.client;
			ScheduledExecutorService pool = this.pool != null ? this.pool : ThreadPools.getDefaultPool(this.id, this.threadFactory, this.isDaemon);
			return new Webhook(this.id, this.token, this.parseMessage, client, pool, this.allowedMentions, this.threadId);
		}
	}
}
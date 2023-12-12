import lombok.SneakyThrows;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Test {

	public static void main(String[] args) throws Throwable {
		var resp = HttpClient.newHttpClient().send(HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:8080/upload/White%20Snake.mp4"))
				.PUT(HttpRequest.BodyPublishers.ofInputStream(() -> getFile("White Snake.mp4")))
				.build(), HttpResponse.BodyHandlers.ofString());
		System.out.println("\n" + resp);
		System.out.println(resp.body());
	}

	@SneakyThrows
	private static InputStream getFile(String name) {
		File file = new File(name);
		long start = System.currentTimeMillis();
		AtomicLong uses = new AtomicLong();
		return new InputStream() {
			private final FileInputStream input = new FileInputStream(file);
			@Override
			public int read() throws IOException {
				int r = input.read();
				if (r != -1) {
					if (uses.incrementAndGet() % 5341 == 0)
						printProgress(start, file.length(), uses.get());
				}
				return r;
			}
		};
	}

	@SneakyThrows
	private static InputStream getInputSteam(long length) {
		long start = System.currentTimeMillis();
		AtomicLong uses = new AtomicLong();
		return new InputStream() {
			private FileInputStream input = new FileInputStream("test.txt");

			@Override
			public int read() throws IOException {
				uses.getAndIncrement();
				int r = input.read();
				if (r == -1 && length - uses.get() > 0) {
					input = new FileInputStream("test.txt");
					r = input.read();
				}
				if (uses.get() % 5341 == 0)
					printProgress(start, length, uses.get());
				return r;
			}
		};
	}

	private static void printProgress(long startTime, long total, long current) {
		long eta = current == 0 ? 0 :
				(total - current) * (System.currentTimeMillis() - startTime) / current;

		String etaHms = current == 0 ? "N/A" :
				String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
						TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
						TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

		int percent = (int) (current * 100 / total);
		String string = '\r' +
				String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")) +
				String.format(" %d%% [", percent) +
				String.join("", Collections.nCopies(percent, "=")) +
				'>' +
				String.join("", Collections.nCopies(100 - percent, " ")) +
				']' +
				String.join("", Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")) +
				String.format(" %d/%d, ETA: %s", current, total, etaHms);

		System.out.print(string);
	}
}

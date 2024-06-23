package de.crazydev22.discordfs;

import de.crazydev22.utils.Cache;
import de.crazydev22.utils.container.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Database {
	private final Cache<Pair<String, String>, DiscordFile> cache = new Cache<>(this::load, 1000, Duration.ofHours(3));
	private final String minPath;

	public Database(@NotNull File storage) {
		this.minPath = storage.getAbsolutePath() + File.separator;
	}

	@Nullable
	public DiscordFile getFile(@NotNull String id, @NotNull String name) {
		return cache.get(new Pair<>(id, name));
	}

	public void deleteFile(@NotNull String id, @NotNull String name) throws IOException {
		DiscordFile discordFile = cache.get(new Pair<>(id, name));
		if (discordFile == null) throw new FileNotFoundException("File not found");
		File file = getFile0(id, name, true);
		if (!file.delete()) throw new IOException("Could not delete file");

		cache.invalidate(new Pair<>(id, name));
		discordFile.getParts()
				.stream()
				.map(DiscordFile.Part::messageID)
				.forEach(Main.getInstance().getWebhook()::delete);
	}

	public void saveFile(@NotNull DiscordFile file) throws IOException {
		File file0 = getFile0(file.getId(), file.getName(), false);
		if (!file0.getParentFile().exists() && !file0.getParentFile().mkdirs())
			throw new IOException("Could not create directory");

		try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(file0)))) {
			dos.writeUTF(file.getToken());
			dos.writeUTF(file.getMime());
			dos.writeInt(file.getParts().size());
			for (var part : file.getParts()) {
				dos.writeLong(part.messageID());
				dos.writeInt(part.ivs().size());
				dos.writeInt(part.partSizes().size());
				for (var iv : part.ivs())
					dos.write(iv);
				for (var size : part.partSizes())
					dos.writeInt(size);
			}
		}
    }

	private File getFile0(String id, String name, boolean exists) {
		if (!id.matches("^[a-zA-Z0-9]*$")) throw new IllegalArgumentException("Id contains invalid characters");
		String basePath = this.minPath + id + File.separator;
		File file = new File(basePath + name);
		if (!file.getAbsolutePath().startsWith(basePath)) throw new IllegalArgumentException("Invalid file path");
		if (exists) {
			if (!file.exists() || !file.isFile()) throw new IllegalArgumentException("File not found");
			return file;
		} else if (file.exists()) throw new IllegalArgumentException("File already exists");
		return file;
	}

	private DiscordFile load(Pair<String, String> pair) {
		File file = getFile0(pair.getA(), pair.getB(), true);
		try (DataInputStream din = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
			String token = din.readUTF();
			String mime = din.readUTF();
			int partCount = din.readInt();
			List<DiscordFile.Part> parts = new ArrayList<>(partCount);
			for (int i = 0; i < partCount; i++) {
				long messageID = din.readLong();
				int ivsCount = din.readInt();
				int sizeCount = din.readInt();
				List<byte[]> ivs = new ArrayList<>(ivsCount);
				List<Integer> sizes = new ArrayList<>(sizeCount);

				for (int j = 0; j < ivsCount; j++) {
					byte[] iv = new byte[16];
					if (din.read(iv) != 16) throw new IOException("Invalid iv");
					ivs.add(iv);
				}

				for (int j = 0; j < sizeCount; j++) {
					sizes.add(din.readInt());
				}
				parts.add(new DiscordFile.Part(messageID, ivs, sizes));
			}
			return new DiscordFile(pair.getA(), token, pair.getB(), mime, parts);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

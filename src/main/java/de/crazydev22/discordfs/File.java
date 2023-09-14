package de.crazydev22.discordfs;

import com.google.gson.JsonObject;
import de.crazydev22.utils.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record File(@NotNull UUID uuid, @Nullable UUID parent, boolean file, @NotNull String name, @NotNull JsonObject data) {
	public static final UUID ROOT_UUID = UUID.fromString("65cefff0-27c7-4414-a92a-4ed1a709f609");
	public static final File ROOT = new File(ROOT_UUID, null, false, "/");

	public File(@NotNull UUID uuid, @Nullable UUID parent, boolean file, @NotNull String name) {
		this(uuid, parent, file, name, new JsonObject());
	}

	public File(@NotNull String uuid, @NotNull String parent, boolean file, @NotNull String name, @NotNull String data) {
		this(UUID.fromString(uuid), parent.isEmpty() ? null : UUID.fromString(parent), file, name, JsonUtil.NORMAL_GSON.fromJson(data, JsonObject.class));
	}

	public List<File> listFiles(@NotNull Database database) {
		return database.listFiles(uuid);
	}
}

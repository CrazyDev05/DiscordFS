package de.crazydev22.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "unused"})
public class JsonConfiguration<T extends JsonElement> {
	protected File file;
	protected final JsonUtil util;
	protected final T defaults;
	protected Callable<JsonElement> creator = JsonObject::new;
	protected T content;

	public JsonConfiguration(@NotNull String fileName, @NotNull T defaults, JsonUtil @NotNull ... util) throws IOException {
		this(new File(fileName), defaults, util);
	}

	public JsonConfiguration(@NotNull final File file, @NotNull final T defaults, JsonUtil @NotNull ... util) throws IOException {
		if (defaults.isJsonNull() || defaults.isJsonPrimitive())
			throw new IllegalArgumentException("The defaults need to be of type JsonObject or JsonArray");

		this.file = file;
		this.util = util.length == 1 ? util[0] : new JsonUtil(true);
		this.defaults = defaults;
		load(file);
	}
	public JsonConfiguration(@NotNull final T content, @NotNull final T defaults, JsonUtil @NotNull ... util) {
		if (defaults.isJsonNull() || defaults.isJsonPrimitive())
			throw new IllegalArgumentException("The defaults need to be of type JsonObject or JsonArray");

		this.util = util.length == 1 ? util[0] : new JsonUtil(true);
		this.content = content;
		this.defaults = defaults;
	}

	@NotNull
	public static JsonConfiguration<JsonObject> fromMap(@NotNull Map<@NotNull String, @Nullable Object> map) {
		JsonConfiguration<JsonObject> config = new JsonConfiguration<>(new JsonObject(), new JsonObject());
		map.forEach(config::set);
		return config;
	}

	@NotNull
	public static JsonConfiguration<JsonObject> fromMap(Object... objects) {
		if (objects.length % 2 != 0) throw new IllegalArgumentException();
		JsonConfiguration<JsonObject> config = new JsonConfiguration<>(new JsonObject(), new JsonObject());
		for (int i = 0; i < objects.length; i+=2)
			config.set((String) objects[i], objects[i+1]);
		return config;
	}

	public void setCreator(@NotNull Callable<JsonElement> creator) {
		this.creator = creator;
	}

	public void load() throws IOException {
		if (file == null)
			throw new IllegalStateException("default file is not specified!");
		load(file);
	}

	public void load(@NotNull File file) throws IOException {
		if (!file.exists()) {
			if (file.getParentFile() != null)
				file.getParentFile().mkdirs();
			util.writeFile(file, defaults);
			content = defaults;
		} else {
			try {
				content = (T) util.readFile(file);
			} catch (Exception e) {
				e.printStackTrace();
				content = defaults;
			}
		}
	}

	public void save() throws IOException {
		if (file == null)
			throw new IllegalStateException("default file is not specified!");
		save(file);
	}

	public void save(@NotNull File file) throws IOException {
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		util.writeFile(file, content);
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void set(@NotNull String path, @NotNull JsonElement element) {
		JsonUtil.set(content, creator, path, element);
	}

	public void set(@NotNull String path, @Nullable Object object) {
		set(path, util.toJson(object));
	}

	@NotNull
	public Optional<JsonElement> get(@NotNull String path) {
		Optional<JsonElement> opt = JsonUtil.get(content, path);
		if (opt.isEmpty())
			opt = JsonUtil.get(defaults, path);
		return opt;
	}

	@NotNull
	public Optional<String> getString(@NotNull String path) {
		return get(path).flatMap(util::asString);
	}

	@NotNull
	public Optional<Integer> getInt(@NotNull String path) {
		return get(path).flatMap(util::asInt);
	}

	@NotNull
	public Optional<Long> getLong(@NotNull String path) {
		return get(path).flatMap(util::asLong);
	}

	@NotNull
	public Optional<Float> getFloat(@NotNull String path) {
		return get(path).flatMap(util::asFloat);
	}

	@NotNull
	public Optional<Double> getDouble(@NotNull String path) {
		return get(path).flatMap(util::asDouble);
	}

	@NotNull
	public Optional<Boolean> getBoolean(@NotNull String path) {
		return get(path).flatMap(util::asBoolean);
	}

	@NotNull
	public <V extends JsonElement> Optional<JsonConfiguration<V>> getConfiguration(@NotNull String path) {
		return get(path).map(element -> {
			V content = (V) element;
			V defaults = element.isJsonObject() ? (V) new JsonObject() : (V) new JsonArray();
			return new JsonConfiguration<>(content, defaults, util);
		});
	}

	@NotNull
	public <V> List<V> getList(@NotNull String path, @NotNull Class<V> clazz) {
		Optional<JsonElement> opt = get(path);
		return opt.map(element -> util.asList(element, clazz))
				.orElseGet(JsonConfiguration::newList);
	}

	@NotNull
	public List<String> getStringList(@NotNull String path) {
		return getList(path, String.class);
	}

	@NotNull
	public List<Long> getLongList(@NotNull String path) {
		Optional<JsonElement> opt = get(path);
		return opt.map(util::asLongList)
				.orElseGet(JsonConfiguration::newList);
	}

	private static <T> List<T> newList() {
		return Collections.synchronizedList(new ArrayList<>());
	}

	public T getContent() {
		return content;
	}

	@Override
	public String toString() {
		return util.toString(content);
	}
}
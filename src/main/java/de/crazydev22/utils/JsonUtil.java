package de.crazydev22.utils;


import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;

@SuppressWarnings("unused")
public class JsonUtil {
	public static final Gson PRETTY_PRINT_GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final Gson NORMAL_GSON = new GsonBuilder().disableHtmlEscaping().create();

	private final Gson gson;

	public JsonUtil() {
		this(NORMAL_GSON);
	}

	public JsonUtil(boolean prettyPrint) {
		this.gson = prettyPrint ? PRETTY_PRINT_GSON : NORMAL_GSON;
	}

	public JsonUtil(Gson gson) {
		this.gson = gson;
	}


	public JsonElement readFile(@NotNull File file) throws FileNotFoundException {
		return readFile(file, gson);
	}

	public static JsonElement readFile(@NotNull File file, @NotNull Gson gson) throws FileNotFoundException {
		return gson.fromJson(new FileReader(file), JsonElement.class);
	}

	public static <T> T readFile(@NotNull File file, @NotNull Gson gson, @NotNull Type type) throws FileNotFoundException {
		return gson.fromJson(new FileReader(file), type);
	}

	public void writeFile(@NotNull File file, @NotNull JsonElement element) throws IOException {
		writeFile(file, element, gson);
	}

	public static void writeFile(@NotNull File file, @NotNull JsonElement element, @NotNull Gson gson) throws IOException {
		FileWriter writer = new FileWriter(file);
		writer.write(gson.toJson(element));
		writer.flush();
		writer.close();
	}

	public static String toString(@NotNull JsonElement element, @NotNull Gson gson) {
		return gson.toJson(element);
	}

	public String toString(@NotNull JsonElement element) {
		return toString(element, gson);
	}

	@NotNull
	public static Optional<JsonElement> get(@NotNull JsonElement element, @NotNull String path) {
		String[] split = path.split("\\.", 2);
		if (split.length == 0)
			return Optional.empty();

		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (!object.has(split[0]))
				return Optional.empty();
			else if (split.length == 2)
				return get(object.get(split[0]), split[1]);
			else
				return Optional.ofNullable(object.get(split[0]));
		} else if (element.isJsonArray()) {
			JsonArray array = element.getAsJsonArray();
			var opt = NumberUtil.parseInteger(split[0]);
			if (opt.isEmpty() || opt.get() >= array.size() || opt.get() < 0)
				return Optional.empty();
			else if (split.length == 2) {
				return get(array.get(opt.get()), split[1]);
			} else {
				return Optional.ofNullable(array.get(opt.get()));
			}
		} else {
			return Optional.empty();
		}
	}

	public static void set(@NotNull JsonElement element, @NotNull Callable<JsonElement> creator, @NotNull String path, JsonElement value) {
		String[] split = path.split("\\.", 2);
		if (split.length == 0)
			return;

		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (split.length == 2) {
				if (!object.has(split[0])) {
					try {
						object.add(split[0], creator.call());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				set(object.get(split[0]), creator, split[1], value);
			} else {
				object.add(split[0], value);
			}
		} else if (element.isJsonArray()) {
			JsonArray array = element.getAsJsonArray();
			var opt = NumberUtil.parseInteger(split[0]);
			if (opt.isEmpty() || opt.get() > array.size() || opt.get() < 0)
				return;

			if (split.length == 2) {
				if (opt.get() == array.size()) {
					try {
						array.add(creator.call());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				set(array.get(opt.get()), creator, split[1], value);
			} else {
				if (opt.get() == array.size())
					array.add(value);
				else
					array.set(opt.get(), value);
			}
		}
	}

	@NotNull
	public static <T> Optional<T> as(@NotNull JsonElement element, Gson gson, Class<T> clazz) {
		try {
			return Optional.ofNullable(gson.fromJson(element, clazz));
		} catch (Exception ignored) {
			return Optional.empty();
		}
	}

	@NotNull
	public JsonElement toJson(@Nullable Object object) {
		return toJson(object, gson);
	}

	@NotNull
	public static JsonElement toJson(@Nullable Object object, Gson gson) {
		return gson.toJsonTree(object);
	}

	@NotNull
	public Optional<String> asString(@NotNull JsonElement element) {
		return as(element, gson, String.class);
	}

	@NotNull
	public Optional<Integer> asInt(@NotNull JsonElement element) {
		return as(element, gson, Integer.class);
	}

	@NotNull
	public Optional<Long> asLong(@NotNull JsonElement element) {
		return as(element, gson, Long.class);
	}

	@NotNull
	public Optional<Float> asFloat(@NotNull JsonElement element) {
		return as(element, gson, Float.class);
	}

	@NotNull
	public Optional<Double> asDouble(@NotNull JsonElement element) {
		return as(element, gson, Double.class);
	}

	@NotNull
	public Optional<Boolean> asBoolean(@NotNull JsonElement element) {
		return as(element, gson, Boolean.class);
	}

	@NotNull
	public static <T> List<T> asList(@NotNull JsonElement element, @NotNull Gson gson, @NotNull Class<T> clazz) {
		List<T> list = Collections.synchronizedList(new ArrayList<>());
		if (!element.isJsonArray())
			return list;
		try {
			for (JsonElement var : element.getAsJsonArray()) {
				try {
					list.add(gson.fromJson(var, clazz));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return list;
		} catch (Exception ignored) {
			return list;
		}
	}

	@NotNull
	public <T> List<T> asList(@NotNull JsonElement element, @NotNull Class<T> clazz) {
		return asList(element, gson, clazz);
	}

	@NotNull
	public List<String> asStringList(@NotNull JsonElement element) {
		return asList(element, gson, String.class);
	}

	@NotNull
	public List<Integer> asIntList(@NotNull JsonElement element) {
		return asList(element, gson, Integer.class);
	}

	@NotNull
	public List<Long> asLongList(@NotNull JsonElement element) {
		return asList(element, gson, Long.class);
	}

	@NotNull
	public List<Float> asFloatList(@NotNull JsonElement element) {
		return asList(element, gson, Float.class);
	}

	@NotNull
	public List<Double> asDoubleList(@NotNull JsonElement element) {
		return asList(element, gson, Double.class);
	}
}
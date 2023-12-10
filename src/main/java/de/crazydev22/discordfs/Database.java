package de.crazydev22.discordfs;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.SQLException;

public class Database {
	private static final String URL = "jdbc:mariadb://%s:%s/%s";
	private final MariaDbDataSource dataSource;

	public Database(String host, int port, String database, String username, String password) throws SQLException {
		dataSource = new MariaDbDataSource(URL.formatted(host, port, database));

		dataSource.setUser(username);
		dataSource.setPassword(password);

		init();
	}

	private void init() throws SQLException {
		try (var connection = dataSource.getConnection()) {
			try (var stmt = connection.createStatement()){
				stmt.setQueryTimeout(10);
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `files` (" +
						"`id` VARCHAR(10) NOT NULL , " +
						"`token` VARCHAR(20) NOT NULL , " +
						"`name` LONGTEXT NOT NULL , " +
						"`mime` TEXT NOT NULL , " +
						"`data` JSON NOT NULL , " +
						"PRIMARY KEY (`id`(10))," +
						"UNIQUE(`id`));");
			}
		}
	}

	@Nullable
	public DiscordFile getFile(@NotNull String id) {
		try (var connection = dataSource.getConnection();
			 var stmt = connection.prepareStatement("SELECT * FROM `files` WHERE `id` = ? LIMIT 1")) {
			stmt.setQueryTimeout(10);
			stmt.setString(1, id);

			var set = stmt.executeQuery();
			if (!set.next())
				return null;

			return new DiscordFile(
					set.getString("id"),
					set.getString("token"),
					set.getString("name"),
					set.getString("mime"),
					set.getString("data"));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteFile(@NotNull String id, @NotNull String token) throws SQLException {
		try (var connection = dataSource.getConnection();
			 var stmt = connection.prepareStatement("DELETE FROM `files` WHERE `id` = ? AND `token` = ? LIMIT 1")) {
			stmt.setQueryTimeout(10);
			stmt.setString(1, id);
			stmt.setString(2, token);

			stmt.executeUpdate();
		}
	}

	public void saveFile(@NotNull DiscordFile file) throws SQLException {
		var tmp = getFile(file.getId());
		if (tmp != null && tmp.getToken().equals(file.getToken()))
			deleteFile(file.getId(), file.getToken());

		try (var connection = dataSource.getConnection();
			 var stmt = connection.prepareStatement("INSERT INTO `files` (id, token, name, mime, data) VALUES (?,?,?,?,?)")) {
			stmt.setQueryTimeout(10);
			stmt.setString(1, file.getId());
			stmt.setString(2, file.getToken());
			stmt.setString(3, file.getName());
			stmt.setString(4, file.getMime());
			stmt.setString(5, Main.GSON.toJson(file.getParts()));

			stmt.executeUpdate();
		}
	}
}

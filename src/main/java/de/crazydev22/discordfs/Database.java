package de.crazydev22.discordfs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
						"`uuid` VARCHAR(36) NOT NULL , " +
						"`parent` VARCHAR(36) NOT NULL , " +
						"`type` BOOL NOT NULL , " +
						"`name` LONGTEXT NOT NULL , " +
						"`data` JSON NOT NULL , " +
						"PRIMARY KEY (`uuid`(36))," +
						"UNIQUE(`uuid`));");
			}
			if (getFile("/") == null) {
				try (var stmt = connection.prepareStatement("INSERT INTO `files` (`uuid`, `parent`, `type`, `name`, `data`) VALUES (?, ?, ?, ?, ?)")) {
					stmt.setString(1, File.ROOT_UUID.toString());
					stmt.setString(2, "");
					stmt.setBoolean(3, false);
					stmt.setString(4, "/");
					stmt.setString(5, "{}");

					stmt.executeUpdate();
				}
			}
		}
	}

	@Nullable
	public File getFile(@NotNull String name) {
		if (name.isEmpty())
			return null;
		if (name.equals("/"))
			return File.ROOT;

		File file = null;
		try (var connection = dataSource.getConnection();
			 var stmt = connection.prepareStatement("SELECT * FROM `files` WHERE `name` = ? LIMIT 1")) {
			stmt.setQueryTimeout(10);
			stmt.setString(1, name);

			var set = stmt.executeQuery();
			if (set.next()) {
				file = new File(
						set.getString("uuid"),
						set.getString("parent"),
						set.getBoolean("type"),
						set.getString("name"),
						set.getString("data"));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return file;
	}

	@NotNull
	public List<File> listFiles(@NotNull UUID parent) {
		List<File> files = new ArrayList<>();
		try (var connection = dataSource.getConnection();
			 var stmt = connection.prepareStatement("SELECT * FROM `files` WHERE parent = ?")) {
			stmt.setQueryTimeout(10);
			stmt.setString(1, parent.toString());

			var set = stmt.executeQuery();
			while (set.next()) {
				files.add(new File(
					set.getString("uuid"),
					set.getString("parent"),
					set.getBoolean("type"),
					set.getString("name"),
					set.getString("data")));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return files;
	}

	public void putFile(@NotNull File file) {
		try (var connection = dataSource.getConnection();
			 var stmt = connection.prepareStatement("INSERT INTO `files` ")) {

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}

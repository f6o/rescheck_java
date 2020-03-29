package rescheck;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
	private static void initDb(String dbFilePath) {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:sample.db")) {
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			stmt.executeUpdate("drop table if exists person");
			stmt.executeUpdate("create table person (id integer, name string)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		if (args.length != 1) {
			System.err.println("no db file specied.");
			System.exit(1);
		}
		initDb(args[0]);
		System.exit(0);
	}
}

package rescheck;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

public class Main {
	private static void initDb(Connection conn) {
		try {
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			stmt.executeUpdate("drop table if exists request");
			stmt.executeUpdate("drop table if exists response");
			stmt.executeUpdate("create table request (id text, url text, method text, headers text, body text)");
			stmt.executeUpdate(
					"create table response (id text, reqid text, status integer, headers text, content_type text, body text)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void printAllRequests(Connection conn) {
		System.out.println("print requests");
		try {
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			ResultSet rs = stmt.executeQuery("select * from request");
			while (rs.next()) {
				System.out.println("id = " + rs.getString("id"));
				System.out.println("url = " + rs.getString("url"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String... args) {
		if (args.length != 1) {
			System.err.println("no db file specied.");
			System.exit(1);
		}
		
		List<RequestDo> requests = new ArrayList<>();
		List<ResponseDo> responses = new ArrayList<>();
		try ( Connection conn = DriverManager.getConnection("jdbc:sqlite:" + args[0])) {
			initDb(conn);
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				for (RequestDo request : requests) {
					request.save(conn);
					ResponseDo response = request.sendWith(httpClient);
					response.save(conn);
					responses.add(response);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				printAllRequests(conn);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		System.exit(0);
	}
}

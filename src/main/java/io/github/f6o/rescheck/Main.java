package io.github.f6o.rescheck;

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
	
	private static void request(String dbFile, String requestFile) {
		final String dbUrl = "jdbc:sqlite:" + dbFile;
		List<RequestDo> requests = RequestDo.createFrom(requestFile);
		if ( requests == null ) {
			System.err.println("file not found: " + requestFile);
			System.exit(2);
		}
		
		List<ResponseDo> responses = new ArrayList<>();
		try ( Connection conn = DriverManager.getConnection(dbUrl)) {
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
	
	private static void printDatabase(String dbFile) {
		// TODO
		System.err.print("TODO: print " + dbFile);
	}
	
	private static void printUsage() {
		System.err.println("usage: java -jar tool.jar <db_file> <request_file>");
	}
	
	public static void main(String... args) {
		if (args.length != 2) {
			printUsage();
			System.exit(1);
		}
		
		// mode
		if ( args[0].startsWith("-") ) {
			String modeName = args[0].substring(1);
			if ( modeName.equals("print") ) {
				printDatabase(args[1]);
			} else {
				printUsage();
				System.exit(1);
			}
		} else {
			request(args[0], args[1]);
		}
		
	}
}

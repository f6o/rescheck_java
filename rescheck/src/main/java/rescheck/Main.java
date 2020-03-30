package rescheck;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class Main {
	private static void initDb(String dbFilePath) {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath)) {
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

	// TODO: Implement this
	private static String insertRequest(String dbFilePath, HttpUriRequestBase request) {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath)) {
			PreparedStatement stmt = conn.prepareStatement("insert into request values (?,?,?,?,?);");
			stmt.setQueryTimeout(30);
			stmt.setString(2, "http://localhost/");
			stmt.setString(3, "get");
			stmt.setString(4, "");
			stmt.setString(5, "");
			
			// TODO: set request id
			stmt.setString(1, "id-0000");
			if ( stmt.execute() ) {
				return "reqid";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static boolean insertResponse(String dbFilePath, String reqid, CloseableHttpResponse response, String body) {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath)) {
			PreparedStatement stmt = conn.prepareStatement("insert into response values (?,?,?,?,?);");
			stmt.setQueryTimeout(30);
			
			stmt.setString(2, reqid);
			stmt.setInt(3, response.getCode());
			stmt.setString(4, Arrays.stream(response.getHeaders()).map(Header::toString).collect(Collectors.joining("\n")));
			stmt.setString(5, body);
			
			// TODO: set response id
			stmt.setString(1, "id-0000");
			
			return stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static void printAllRequests(String dbFilePath) {
		System.out.println("print requests");
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath)) {
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
	
	// TODO: Implement this!
	private static HttpUriRequestBase makeRequest(File requestFile) {
		return null;
	}

	public static void main(String... args) {
		if (args.length != 1) {
			System.err.println("no db file specied.");
			System.exit(1);
		}
		initDb(args[0]);

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpUriRequestBase httpRequest = makeRequest(null);
			String reqid = insertRequest(args[0], httpRequest);
			if ( reqid == null ) {
				System.err.println("failed at inserting request to db");
			}

			try (CloseableHttpResponse response = httpclient.execute(httpRequest)) {
				String body = EntityUtils.toString(response.getEntity());
				insertResponse(args[0], reqid, response, body);
			} catch (ParseException e) {
				System.err.println("failed at inserting response to db");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			printAllRequests(args[0]);
		}

		System.exit(0);
	}
}

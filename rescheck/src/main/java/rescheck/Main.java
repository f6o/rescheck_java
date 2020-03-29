package rescheck;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

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

	private static boolean insertRequest(String dbFilePath, HttpUriRequestBase request) {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath)) {
			PreparedStatement stmt = conn.prepareStatement("insert into request values (?,?,?,?,?);");
			stmt.setQueryTimeout(30);
			stmt.setString(1, "id-0000");
			stmt.setString(2, "http://localhost/");
			stmt.setString(3, "get");
			stmt.setString(4, "");
			stmt.setString(5, "");
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

	public static void main(String... args) {
		if (args.length != 1) {
			System.err.println("no db file specied.");
			System.exit(1);
		}
		initDb(args[0]);

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpGet httpGet = new HttpGet("http://httpbin.org/get");
			if ( !insertRequest(args[0], httpGet) ) {
				System.err.println("failed at inserting request to db");
			}

			try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
				System.out.println(response1.getCode() + " " + response1.getReasonPhrase());
				HttpEntity entity1 = response1.getEntity();
				EntityUtils.consume(entity1);
			}

			HttpPost httpPost = new HttpPost("http://httpbin.org/post");
			List<NameValuePair> nvps = new ArrayList<>();
			nvps.add(new BasicNameValuePair("username", "vip"));
			nvps.add(new BasicNameValuePair("password", "secret"));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));

			try (CloseableHttpResponse response2 = httpclient.execute(httpPost)) {
				System.out.println(response2.getCode() + " " + response2.getReasonPhrase());
				HttpEntity entity2 = response2.getEntity();
				EntityUtils.consume(entity2);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			printAllRequests(args[0]);
		}

		System.exit(0);
	}
}

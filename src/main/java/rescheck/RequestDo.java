package rescheck;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class RequestDo extends BaseDo implements DBStorable {
	private String url;
	private String method;

	HttpUriRequestBase toHttpRequest() throws ProtocolException {
		HttpUriRequestBase request;
		if ( method.equalsIgnoreCase("get") ) {
			request = new HttpGet(url);
		} else {
			request = new HttpPost(url);
		}
		for ( String headerLine : this.headers ) {
			String[] kvPair = headerLine.split("[:]", 2);
			request.addHeader(kvPair[0], kvPair[1]);
		}
		if ( body != null ) {
			// TODO: set content-type and charset
			HttpEntity entity = new StringEntity(body);
			request.setEntity(entity);
		}
		
		return request;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	protected void setHash() {
		this.hash = calcHash(url, method, headers, body);
	}

	public ResponseDo sendWith(CloseableHttpClient httpClient) {
		try {
			CloseableHttpResponse resp = httpClient.execute(this.toHttpRequest());
			return new ResponseDo(this.getHash(), resp);
		} catch (Exception e) {
			return new ResponseDo(e);
		}
	}
	
	@Override
	public PreparedStatement setParameter(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setString(1, getHash());
		preparedStatement.setString(2, url);
		preparedStatement.setString(3, method);
		preparedStatement.setString(4, headers.stream().collect(Collectors.joining("\n")));
		preparedStatement.setString(5, body == null ? "" : body);
		return preparedStatement;
	}

	@Override
	public boolean save(Connection conn) {
		try {
			PreparedStatement stmt = conn.prepareStatement("insert into request values (?,?,?,?,?);");
			stmt.setQueryTimeout(30);
			setParameter(stmt);
			return stmt.execute();
		} catch ( SQLException e ) {
			return false;
		}
	}
	
	private static Pattern DELIMITER = Pattern.compile("^---+(\\r?\\n)?", Pattern.MULTILINE); 

	// TODO: implement this
	public static final RequestDo parseRequest(String text) {
		System.err.println("<parse>" + text);
		return new RequestDo();
	}
	
	public static final List<RequestDo> createFrom(String filePath) {
		List<RequestDo> requests = new ArrayList<>();
		try ( final Scanner scanner = new Scanner(new File(filePath)) ) {
			scanner.useDelimiter(DELIMITER);
			while ( scanner.hasNext() ) {
				final String text = scanner.next();
				final RequestDo rdo = RequestDo.parseRequest(text);
				requests.add(rdo);
			}
		} catch (FileNotFoundException e) {
			return null;
		}
		return requests;
	}

}

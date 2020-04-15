package io.github.f6o.rescheck;

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
	private String parseErrorMessage;

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
	
	public boolean parsed() {
		return this.parseErrorMessage != null;
	}
	
	private void setParseError(String msg) {
		this.parseErrorMessage = msg;
	}

	@Override
	protected void setHash() {
		if ( body == null )
			this.hash = calcHash(url, method, headers);
		else
			this.hash = calcHash(url, method, headers, body);
	}
	
	@Override
	public String toString() {
		final String bodyInfo = this.getBody() == null ? "" : " len="+this.getBody().length(); 
		return String.format("[id=%s %s %s%s]", this.getHash(), this.getMethod(), this.getUrl(), bodyInfo);
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
	
	private final static String CRLF= "\r\n";

	/***
	 * 
	 * @param text LF \n assumed
	 * @return
	 */
	public static final RequestDo parseRequest(String text) {
		System.err.println("<parse>" + text);
		RequestDo request = new RequestDo();
		try ( final Scanner scanner = new Scanner(text)  ) {
			scanner.useDelimiter(CRLF);
			System.err.println("parse request: delim=" + scanner.delimiter());
			
			if ( !scanner.hasNextLine() ) {
				request.setParseError("first line not found");
			}

			// GET http://localhost/index.html
			String line = scanner.nextLine();
			System.err.println(line);
			String[] firstLine = line.split("\\s+");
			request.setMethod(firstLine[0]);
			request.setUrl(firstLine[1]);
			
			boolean hasBody = false;

			// x-sample-header: value1
			while ( scanner.hasNextLine() ) {
				final String headerLine = scanner.nextLine();
				if ( headerLine.isEmpty() ) {
					hasBody = true;
					break;
				} else {
					request.addHeader(headerLine);
				}
			}
			
			// rest, if any, is for body
			if ( hasBody ) {
				StringBuilder sb = new StringBuilder();
				if ( scanner.hasNextLine() ) {
					sb.append(scanner.nextLine());
				}
				while ( scanner.hasNextLine() ) {
					sb.append(CRLF);
					sb.append(scanner.nextLine());
				}
				request.setBody(sb.toString());
			}
		}
		System.err.println("req: " + request);
		return request;
	}

	private final static Pattern REQUEST_CHUNK_DELIMITER = Pattern.compile("^---+\r\n", Pattern.MULTILINE);
	
	public static final List<RequestDo> createFrom(String filePath) {
		List<RequestDo> requests = new ArrayList<>();
		try ( final Scanner scanner = new Scanner(new File(filePath)) ) {
			scanner.useDelimiter(REQUEST_CHUNK_DELIMITER);
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

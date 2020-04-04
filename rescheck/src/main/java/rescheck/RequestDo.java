package rescheck;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
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

	@Override
	public PreparedStatement setParameter(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setString(1, getHash());
		preparedStatement.setString(2, url);
		preparedStatement.setString(3, method);
		preparedStatement.setString(4, headers.stream().collect(Collectors.joining("\n")));
		preparedStatement.setString(5, body == null ? "" : body);
		return preparedStatement;
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

	// TODO: Implement this
	public ResponseDo sendWith(CloseableHttpClient httpClient) {
		return null;
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

}

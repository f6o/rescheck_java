package rescheck;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class ResponseDo extends BaseDo implements DBStorable {
	private String requestId;
	private Integer status;
	private String contentType;
	private Exception exceptionWhileRequesting;

	public ResponseDo(String requestId, ClassicHttpResponse response) {
		setRequestId(requestId);
		setStatus(response.getCode());

		for ( Header h : response.getHeaders() ) {
			addHeader(h.getName() + ": " + h.getValue());
		}
		
		try {
			Header h = response.getHeader("content-type");
			setContentType(h.getValue());
		} catch (ProtocolException e) {
			setContentType(null);
		}

		HttpEntity entity = response.getEntity();
		try {
			String body = EntityUtils.toString(entity);
			setBody(body);
		} catch (ParseException e) {
		} catch (IOException e) {
		}
	}

	public ResponseDo(Exception e) {
		this.exceptionWhileRequesting = e;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public Exception getExceptionWhileRequesting() {
		return exceptionWhileRequesting;
	}

	@Override
	protected void setHash() {
		this.hash = calcHash(requestId, status, headers, contentType, body);
	}
	
	@Override
	public PreparedStatement setParameter(PreparedStatement preparedStatement) throws SQLException {
		preparedStatement.setString(1, getHash());
		preparedStatement.setString(2, requestId);
		preparedStatement.setInt(3, status);
		preparedStatement.setString(4, headers.stream().collect(Collectors.joining("\n")));
		preparedStatement.setString(5, contentType);
		preparedStatement.setString(6, body);
		return preparedStatement;
	}

	@Override
	public boolean save(Connection conn) {
		try {
			PreparedStatement stmt = conn.prepareStatement("insert into response values (?,?,?,?,?,?);");
			stmt.setQueryTimeout(30);
			setParameter(stmt);
			return stmt.execute();
		} catch ( SQLException e ) {
			return false;
		}
	}
}

package rescheck;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class ResponseDo extends BaseDo {
	private String requestId;
	private Integer status;
	private String contentType;
	
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

	@Override
	protected void setHash() {
		this.hash = calcHash(requestId, status, headers, contentType, body);
	}
}

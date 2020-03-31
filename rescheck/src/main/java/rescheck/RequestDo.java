package rescheck;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class RequestDo extends BaseDo {
	private String url;
	private String method;

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

}

package rescheck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DBStorable {
	public boolean save(Connection conn);
	public PreparedStatement setParameter(PreparedStatement preparedStatement) throws SQLException;
}

package cm.model;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DataInputMapper implements RowMapper<DataInput> {
    @Override
    public DataInput mapRow(ResultSet rs, int rowNum) throws SQLException {
        return null;
    }
}

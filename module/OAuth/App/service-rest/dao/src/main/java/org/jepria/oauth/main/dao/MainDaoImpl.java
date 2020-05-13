package org.jepria.oauth.main.dao;

import com.technology.jep.jepria.server.dao.ResultSetMapper;
import org.jepria.oauth.main.OperatorOptions;
import org.jepria.server.data.DaoSupport;
import org.jepria.server.data.OptionDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MainDaoImpl implements MainDao {
  @Override
  public List<OptionDto<String>> getOperators(String operatorName, Integer maxRowCount) {
    String sqlQuery =
        " begin "
            + " ? := pkg_operator.getOperator("
            + "operatorName => ?"
            + ", maxRowCount => ? "
            + ");"
            + " end;";
  
    return DaoSupport.getInstance().find(
        sqlQuery,
        new ResultSetMapper<OptionDto<String>>() {
          public void map(ResultSet rs, OptionDto<String> dto) throws SQLException {
            dto.setValue(rs.getString(OperatorOptions.OPERATOR_ID));
            dto.setName(rs.getString(OperatorOptions.OPERATOR_NAME));
          }
        },
        OptionDto.class,
        operatorName + "%",
        maxRowCount
    );
  }
}

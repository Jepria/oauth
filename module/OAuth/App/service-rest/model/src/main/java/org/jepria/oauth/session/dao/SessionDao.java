package org.jepria.oauth.session.dao;

import org.jepria.server.data.Dao;
import org.jepria.server.data.OptionDto;

import java.util.List;

public interface SessionDao extends Dao {
  List<OptionDto<String>> getOperators(String operatorName, Integer maxRowCount);
}

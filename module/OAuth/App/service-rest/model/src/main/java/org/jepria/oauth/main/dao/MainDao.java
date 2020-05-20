package org.jepria.oauth.main.dao;

import org.jepria.server.data.OptionDto;

import java.util.List;

public interface MainDao {
  List<OptionDto<String>> getOperators(String operatorName, Integer maxRowCount);
}

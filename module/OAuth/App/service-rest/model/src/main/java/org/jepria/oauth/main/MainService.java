package org.jepria.oauth.main;

import org.jepria.server.data.OptionDto;

import java.util.List;

public interface MainService {
  List<OptionDto<String>> getOperators(String operatorName, Integer maxRowCount);
}

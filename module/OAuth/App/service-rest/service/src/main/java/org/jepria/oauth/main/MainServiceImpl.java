package org.jepria.oauth.main;

import org.jepria.oauth.main.dao.MainDao;
import org.jepria.server.data.OptionDto;

import java.util.List;

public class MainServiceImpl implements MainService {
  
  private MainDao dao;
  
  public MainServiceImpl(MainDao dao) {
    this.dao = dao;
  }
  
  @Override
  public List<OptionDto<String>> getOperators(String operatorName, Integer maxRowCount) {
    return dao.getOperators(operatorName, maxRowCount);
  }
}

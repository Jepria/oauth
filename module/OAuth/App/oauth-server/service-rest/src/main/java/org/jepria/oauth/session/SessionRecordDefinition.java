package org.jepria.oauth.session;

import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.RecordDefinitionDtoImpl;

import java.util.Comparator;

import static org.jepria.oauth.session.SessionFieldNames.OPERATOR_NAME;

public class SessionRecordDefinition extends RecordDefinitionDtoImpl {

  public SessionRecordDefinition() {
    super(SessionSearchDto.class, SessionDto.class);
  }
  
  @Override
  public Comparator<Object> getFieldComparator(String fieldName) {
    switch (fieldName) {
      case "operatorName": {
        return Comparator.comparing(o -> ((OptionDto) o).getName());
      }
      case "operatorId": {
        return Comparator.comparing(o -> ((OptionDto<Integer>) o).getValue());
      }
      case "clientName": {
        return Comparator.comparing(o -> ((OptionDto) o).getName());
      }
      case "clientId": {
        return Comparator.comparing(o -> ((OptionDto<String>) o).getValue());
      }
      default: return null;
    }
  }
}

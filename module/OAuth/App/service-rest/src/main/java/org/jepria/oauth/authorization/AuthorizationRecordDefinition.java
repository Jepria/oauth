package org.jepria.oauth.authorization;

import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDtoLocal;
import org.jepria.server.data.RecordDefinitionDtoImpl;

public class AuthorizationRecordDefinition extends RecordDefinitionDtoImpl {

  public AuthorizationRecordDefinition() {
    super(AuthRequestSearchDtoLocal.class, AuthRequestDto.class);
  }
//
//  private final Set<String> fieldNames;
//
//  {
//    Set<String> fieldNames = new HashSet<>();
//
//    fieldNames.add(AuthorizationFieldNames.AUTH_REQUEST_ID);
//    fieldNames.add(AuthorizationFieldNames.AUTHORIZATION_CODE);
//    fieldNames.add(AuthorizationFieldNames.DATE_INS);
//    fieldNames.add(AuthorizationFieldNames.OPERATOR_ID);
//    fieldNames.add(AuthorizationFieldNames.OPERATOR_NAME);
//    fieldNames.add(AuthorizationFieldNames.OPERATOR_LOGIN);
//    fieldNames.add(AuthorizationFieldNames.CLIENT_ID);
//    fieldNames.add(AuthorizationFieldNames.CLIENT_NAME);
//    fieldNames.add(AuthorizationFieldNames.TOKEN_ID);
//    fieldNames.add(AuthorizationFieldNames.TOKEN_DATE_INS);
//    fieldNames.add(AuthorizationFieldNames.IS_BLOCKED);
//
//    this.fieldNames = Collections.unmodifiableSet(fieldNames);
//  }
//
//  @Override
//  public Set<String> getFieldNames() {
//    return fieldNames;
//  }
//
//  private final Map<String, Class<?>> fieldTypes;
//  {
//    Map<String, Class<?>> fieldTypes = new HashMap<>();
//
//    fieldTypes.put(AuthorizationFieldNames.AUTH_REQUEST_ID, Integer.class);
//    fieldTypes.put(AuthorizationFieldNames.AUTHORIZATION_CODE, String.class);
//    fieldTypes.put(AuthorizationFieldNames.DATE_INS, Date.class);
//    fieldTypes.put(AuthorizationFieldNames.OPERATOR_ID, Integer.class);
//    fieldTypes.put(AuthorizationFieldNames.OPERATOR_NAME, String.class);
//    fieldTypes.put(AuthorizationFieldNames.OPERATOR_LOGIN, String.class);
//    fieldTypes.put(AuthorizationFieldNames.CLIENT_ID, String.class);
//    fieldTypes.put(AuthorizationFieldNames.CLIENT_NAME, String.class);
//    fieldTypes.put(AuthorizationFieldNames.TOKEN_ID, Integer.class);
//    fieldTypes.put(AuthorizationFieldNames.TOKEN_DATE_INS, Date.class);
//    fieldTypes.put(AuthorizationFieldNames.IS_BLOCKED, Boolean.class);
//
//    this.fieldTypes = Collections.unmodifiableMap(fieldTypes);
//  }
//
//  @Override
//  public Class<?> getFieldType(String fieldName) {
//    return fieldTypes.get(fieldName);
//  }
//
//
//  private final List<String> primaryKey;
//  {
//    this.primaryKey = Collections.unmodifiableList(Arrays.asList(AuthorizationFieldNames.AUTH_REQUEST_ID));
//  }
//
//  @Override
//  public List<String> getPrimaryKey() {
//    return primaryKey;
//  }
}

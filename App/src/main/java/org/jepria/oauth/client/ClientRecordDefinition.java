package org.jepria.oauth.client;

import org.jepria.server.data.RecordDefinition;

import java.util.*;

public class ClientRecordDefinition implements RecordDefinition {

  public ClientRecordDefinition() {}

  private final Set<String> fieldNames;
  {
    Set<String> fieldNames = new HashSet<>();

   fieldNames.add(ClientFieldNames.CLIENT_ID);

    this.fieldNames = Collections.unmodifiableSet(fieldNames);
  }

  @Override
  public Set<String> getFieldNames() {
    return fieldNames;
  }

  private final Map<String, Class<?>> fieldTypes;
  {
    Map<String, Class<?>> fieldTypes = new HashMap<>();

    fieldTypes.put(ClientFieldNames.CLIENT_ID, Integer.class);

    this.fieldTypes = Collections.unmodifiableMap(fieldTypes);
  }

  @Override
  public Class<?> getFieldType(String fieldName) {
    return fieldTypes.get(fieldName);
  }

  private final List<String> primaryKey = Collections.unmodifiableList(Arrays.asList(ClientFieldNames.CLIENT_ID));

  @Override
  public List<String> getPrimaryKey() {
    return primaryKey;
  }
}

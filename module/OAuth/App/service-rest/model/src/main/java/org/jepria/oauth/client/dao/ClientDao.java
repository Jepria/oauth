package org.jepria.oauth.client.dao;

import org.jepria.server.data.Dao;
import org.jepria.server.data.OptionDto;

import java.util.List;

public interface ClientDao extends Dao {
  List<String> getClientGrantTypes(String clientId);
}

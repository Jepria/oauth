package org.jepria.oauth.authentication.dao;

import org.jepria.server.data.Dao;

public interface AuthenticationDao extends Dao {
  public Integer loginByPassword(String username, String password);

  Integer loginByClientCredentials(String clientId, String clientSecret);
}

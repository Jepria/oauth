package org.jepria.oauth.authorization.dao;

import org.jepria.server.data.Dao;

import java.util.Map;

public interface AuthorizationDao extends Dao {
  void blockAuthRequest(Integer authRequestId);
}

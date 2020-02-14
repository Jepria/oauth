package org.jepria.oauth.authentication.dao;

import org.jepria.server.data.Dao;

public interface AuthenticationDao extends Dao {
  Integer loginByPassword(String username, String password);
  Integer loginByClientCredentials(String clientId, String clientSecret);
  Boolean verifyPKCE(String authorizationCode, String codeVerifier);
}

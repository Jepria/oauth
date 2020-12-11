package org.jepria.oauth.authentication.dao;


public interface AuthenticationDao {
  Integer loginByPassword(String username, String password);
  Integer loginByHash(String username, String passwordHash);
  Integer loginByClientSecret(String clientId, String clientSecret);
  Boolean verifyPKCE(String authorizationCode, String codeVerifier);
}

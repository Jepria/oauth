package org.jepria.oauth.authentication.dao;


public interface AuthenticationDao {
  Integer loginByPassword(String username, String password);
  Integer loginByClientSecret(String clientId, String clientSecret);
  Boolean verifyPKCE(String authorizationCode, String codeVerifier);
}

package org.jepria.oauth.model.authorization;

import org.jepria.oauth.model.session.dto.SessionDto;

public interface AuthorizationService {
  
  SessionDto authorize(String responseType,
                       String clientId,
                       String redirectUri,
                       String codeChallenge);
  
  SessionDto authorize(String responseType,
                       String clientId,
                       String redirectUri,
                       String codeChallenge,
                       String sessionToken,
                       String issuer,
                       String publicKey,
                       String privateKey);
  
  void logout(String clientId,
              String redirectUri,
              String sessionToken,
              String issuer,
              String publicKey,
              String privateKey);
}

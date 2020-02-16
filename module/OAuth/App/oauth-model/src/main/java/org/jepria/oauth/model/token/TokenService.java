package org.jepria.oauth.model.token;

import org.jepria.oauth.model.token.dto.TokenDto;
import org.jepria.oauth.model.token.dto.TokenInfoDto;
import org.jepria.server.service.security.Credential;

public interface TokenService {
  
  /**
   * @param responseType
   * @param privateKey
   * @param host
   * @param authCode
   * @param clientId
   * @param redirectUri
   * @return
   */
  TokenDto create(String responseType,
                  String privateKey,
                  String host,
                  String authCode,
                  String clientId,
                  String redirectUri);
  
  /**
   * @param grantType
   * @param privateKey
   * @param host
   * @param authCode
   * @param clientId
   * @param redirectUri
   * @return
   */
  TokenDto create(String grantType,
                  String publicKey,
                  String privateKey,
                  String host,
                  String authCode,
                  String clientId,
                  String clientSecret,
                  String codeVerifier,
                  String redirectUri,
                  String username,
                  String password,
                  String refreshToken);
  
  /**
   * @param publicKey
   * @param hostContext
   * @param tokenString
   * @return
   */
  TokenInfoDto getTokenInfo(String publicKey, String hostContext, String tokenString, Credential credential);
  
  /**
   * @param clientId
   * @param tokenString
   * @return
   */
  void deleteToken(String clientId, String tokenString, Credential credential);
  
}

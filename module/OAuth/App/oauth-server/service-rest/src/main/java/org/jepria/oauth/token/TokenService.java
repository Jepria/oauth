package org.jepria.oauth.token;

import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;

import java.net.URI;

public interface TokenService {


  /**
   * create token for authorization code flow (response_type=code/token, grant_type=authorization_code)
   *
   * @param clientId client application id
   * @param authCode otp
   * @param redirectUri client redirect uri
   * @param issuer issuer server name
   * @param accessTokenLifeTime life time of access token in seconds
   * @param refreshTokenLifeTime life time of refresh token in seconds
   * @return token
   */
  TokenDto create(String clientId,
                  String authCode,
                  String issuer,
                  URI redirectUri,
                  Long accessTokenLifeTime,
                  Long refreshTokenLifeTime);
  
  /**
   * create token for user password flow (grant_type=password)
   *
   * @param clientId client application id
   * @param username user name
   * @param userId user id
   * @param issuer issuer server name
   * @param accessTokenLifeTime life time of access token in seconds
   * @param refreshTokenLifeTime life time of refresh token in seconds
   * @return token
   */
  TokenDto create(String clientId,
                  String username,
                  Integer userId,
                  String issuer,
                  Long accessTokenLifeTime,
                  Long refreshTokenLifeTime);
  
  /**
   * create token for refresh token flow (grant_type=refresh_token)
   *
   * @param clientId client application id
   * @param refreshToken refresh токен
   * @param issuer issuer server name
   * @param accessTokenLifeTime life time of access token in seconds
   * @param refreshTokenLifeTime life time of refresh token in seconds
   * @return token
   */
  TokenDto create(String clientId,
                  String refreshToken,
                  String issuer,
                  Long accessTokenLifeTime,
                  Long refreshTokenLifeTime);
  
  /**
   * create token for client credentials flow (grant_type=client_credentials)
   *
   * @param clientId client application id
   * @param clientOperatorId client application operator id
   * @param issuer issuer server name
   * @param accessTokenLifeTime life time of access token in seconds
   * @param refreshTokenLifeTime life time of refresh token in seconds
   * @return token
   */
  TokenDto create(String clientId,
                  Integer clientOperatorId,
                  String issuer,
                  Long accessTokenLifeTime,
                  Long refreshTokenLifeTime);
  
  /**
   * get token info
   *
   * @param issuer issuer server name
   * @param tokenString token
   * @return token info meta
   */
  TokenInfoDto getTokenInfo(String issuer, String tokenString);
  
  /**
   * delete issued token
   *
   * @param clientId client application id
   * @param tokenString token
   */
  void delete(String clientId, String tokenString);
  
}

package org.jepria.oauth.token;

import org.jepria.oauth.authentication.AuthenticationServerFactory;
import org.jepria.oauth.authentication.AuthenticationService;
import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.authorization.dto.AuthRequestCreateDto;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDtoLocal;
import org.jepria.oauth.authorization.dto.AuthRequestUpdateDto;
import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.sdk.GrantType;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.oauth.token.dto.TokenInfoDto;

import org.jepria.oauth.sdk.token.interfaces.Signer;
import org.jepria.oauth.sdk.token.interfaces.Token;
import org.jepria.oauth.sdk.token.interfaces.Verifier;
import org.jepria.oauth.sdk.token.SignerRSA;
import org.jepria.oauth.sdk.token.TokenImpl;
import org.jepria.oauth.sdk.token.VerifierRSA;

import javax.security.auth.login.LoginException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TokenService {

  private AuthRequestDto getAuthRequest(String authCode, String clientId, String redirectUri, String tokenId)  {
    AuthRequestSearchDtoLocal searchTemplate = new AuthRequestSearchDtoLocal();
    searchTemplate.setAuthorizationCode(authCode);
    searchTemplate.setClientId(clientId);
    searchTemplate.setRedirectUri(redirectUri);
    searchTemplate.setTokenId(tokenId);
    List<AuthRequestDto> result = (List<AuthRequestDto>) AuthorizationServerFactory.getInstance().getDao().find(searchTemplate, 1);
    if (result.size() == 1) {
      return result.get(0);
    } else {
      throw new NoSuchElementException("Authorization request not found");
    }
  }

  private void updateAuthRequest(Integer authRequestId, Integer operatorId, String tokenId, Date tokenDateIns) {
    AuthRequestUpdateDto updateDto = new AuthRequestUpdateDto();
    updateDto.setAuthRequestId(authRequestId);
    updateDto.setOperatorId(operatorId);
    updateDto.setTokenId(tokenId);
    updateDto.setTokenDateIns(tokenDateIns);
    AuthorizationServerFactory.getInstance().getService().update(updateDto);
  }


  private ClientDto getClient(String clientId) throws IllegalArgumentException {
    ClientSearchDto clientSearchTemplate = new ClientSearchDto();
    clientSearchTemplate.setClientId(clientId);
    List<ClientDto> result = (List<ClientDto>) ClientServerFactory.getInstance().getDao().find(clientSearchTemplate, 1);
    if (result.size() == 1) {
      return result.get(0);
    } else {
      throw new NoSuchElementException("Client not found");
    }
  }

  /**
   *
   * @param grantType
   * @param privateKey
   * @param host
   * @param authCode
   * @param clientId
   * @param redirectUri
   * @return
   */
  public Response create(
    String grantType,
    String privateKey,
    String host,
    String authCode,
    String clientId,
    String redirectUri,
    String username,
    String password) {
    Response response = null;
    try {
      switch (grantType) {
        case GrantType.AUTHORIZATION_CODE: {
          TokenDto result = createForAuthCodeGrant(privateKey, host, authCode, clientId, new String(Base64.getUrlDecoder().decode(redirectUri)));
          response = Response.ok().entity(result).build();
          break;
        }
        case GrantType.CLIENT_CREDENTIALS: {
          break;
        }
        case GrantType.PASSWORD: {
          TokenDto result = createForUserCredentialsGrant(privateKey, host, clientId, username, password);
          response = Response.ok().entity(result).build();
          break;
        }
        default: {
          response = Response.status(Response.Status.BAD_REQUEST).build();
          break;
        }
      }
    } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
      response = Response.status(Response.Status.BAD_REQUEST).build();
    } catch (Throwable th) {
      response = Response.serverError().build();
    } finally {
      return response;
    }
  }

  /**
   *
   * @param privateKeyString
   * @param host
   * @param authCode
   * @param clientId
   * @param redirectUri
   * @return
   */
  public TokenDto createForImplicitGrant(String privateKeyString, String host, String authCode, String clientId, String redirectUri) {
    if (authCode == null) {
      throw new IllegalArgumentException("Authorization code is null.");
    }
    if (clientId == null) {
      throw new IllegalArgumentException("Client ID is null.");
    }
    if (redirectUri == null) {
      throw new IllegalArgumentException("Redirect URI is null.");
    }

    AuthRequestDto authRequest = getAuthRequest(authCode, clientId, redirectUri, null);
    if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - authRequest.getDateIns().getTime()) > 10) {
      throw new IllegalStateException("Authorization code active time has expired.");
    }
    if (authRequest.getTokenId() != null) {
      throw new IllegalStateException("Request is finished.");
    }
    Token token = generateToken(authRequest.getOperatorLogin(), null, authRequest.getOperator().getValue(), host, privateKeyString, 1);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(token.asString());
    tokenDto.setExpiresIn(3600); // 1 hour active time
    tokenDto.setTokenType("Bearer");
    updateAuthRequest(authRequest.getAuthRequestId(), authRequest.getOperator().getValue(), token.getJti(), new Date());
    return tokenDto;
  }

  /**
   *
   * @param privateKeyString
   * @param host
   * @param authCode
   * @param clientId
   * @param redirectUri
   * @return
   */
  private TokenDto createForAuthCodeGrant(String privateKeyString, String host, String authCode, String clientId, String redirectUri) {
    if (authCode == null) {
      throw new IllegalArgumentException("Authorization code is null.");
    }
    if (clientId == null) {
      throw new IllegalArgumentException("Client ID is null.");
    }
    if (redirectUri == null) {
      throw new IllegalArgumentException("Redirect URI is null.");
    }
    AuthRequestDto authRequest = getAuthRequest(authCode, clientId, redirectUri, null);
    if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - authRequest.getDateIns().getTime()) > 10) {
      throw new IllegalStateException("Authorization code active time has expired.");
    }
    if (authRequest.getTokenId() != null) {
      throw new IllegalStateException("Request is finished.");
    }
    Token token = generateToken(authRequest.getOperatorLogin(), null, authRequest.getOperator().getValue(), host, privateKeyString, 4);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccessToken(token.asString());
    tokenDto.setExpiresIn(14400); // 4 hours active time
    tokenDto.setTokenType("Bearer");
    updateAuthRequest(authRequest.getAuthRequestId(), authRequest.getOperator().getValue(), token.getJti(), new Date());
    return tokenDto;
  }

  /**
   *
   * @param privateKeyString
   * @param host
   * @param username
   * @param password
   * @return
   */
  private TokenDto createForUserCredentialsGrant(String privateKeyString, String host, String clientId, String username, String password) {
    TokenDto result = null;

    try {
      Integer operatorId = AuthenticationServerFactory.getInstance().getService().loginByPassword(username, password);

      Token token = generateToken(username, Collections.singletonList(clientId), operatorId, host, privateKeyString, 1);
      TokenDto tokenDto = new TokenDto();
      tokenDto.setAccessToken(token.asString());
      tokenDto.setExpiresIn(3600); // 1 hours active time
      tokenDto.setTokenType("Bearer");

      AuthRequestCreateDto createDto = new AuthRequestCreateDto();
      createDto.setAuthorizationCode(AuthorizationServerFactory.getInstance().getService().generateCode());
      createDto.setClientId(clientId);
      createDto.setOperatorId(operatorId);
      createDto.setTokenId(token.getJti());
      createDto.setTokenDateIns(new Date());
      AuthorizationServerFactory.getInstance().getService().create(createDto);

      result = tokenDto;
    } catch (LoginException e) {
      e.printStackTrace();
    }
    return result;
  }

  /**
   *
   * @param privateKeyString
   * @param host
   * @param clientId
   * @param clientSecret
   * @return
   */
  private TokenDto createForClientCredentialsGrant(String privateKeyString, String host, String clientId, String clientSecret) {
    return null;
  }

  /**
   *
   * @param publicKey
   * @param hostContext
   * @param tokenString
   * @return
   */
  public TokenInfoDto getTokenInfo(String publicKey, String hostContext, String tokenString) {
    TokenInfoDto result = new TokenInfoDto();
    Token token;
    try {
      token = TokenImpl.parseFromString(tokenString);
      AuthRequestDto authRequestDto = getAuthRequest(null, null, null, token.getJti());
      if (authRequestDto.getBlocked()) {
        result.setActive(false);
        return result;
      }
      Verifier verifier = new VerifierRSA(null, hostContext, new Date(), publicKey);
      if (!verifier.verify(token)) {
        result.setActive(false);
        return result;
      }
    } catch (Throwable e) {
      e.printStackTrace();
      result.setActive(false);
      return result;
    }
    result.setActive(true);
    result.setJti(token.getJti());
    result.setAud(token.getAudience());
    result.setIss(token.getIssuer());
    result.setExp(token.getExpiryDate().getTime());
    result.setSub(token.getSubject());
    return result;
  }

  /**
   *
   * @param clientId
   * @param tokenString
   * @param redirectUri
   * @return
   */
  public Response revokeToken(String clientId, String tokenString, String redirectUri) {
    if (redirectUri != null) {
      try {
        URI redirectionURI = URI.create(new String(Base64.getUrlDecoder().decode(redirectUri)));
        return  Response.status(302).location(redirectionURI).build();
      } catch (IllegalArgumentException ex) {
        ex.printStackTrace();
      }
    }
    Response response = null;
    try {
      Token token = TokenImpl.parseFromString(tokenString);
      AuthRequestDto authRequestDto = getAuthRequest(null, null, null, token.getJti());
      AuthorizationServerFactory.getInstance().getDao().blockAuthRequest(authRequestDto.getAuthRequestId());
      response = Response.ok().build();
    } catch (IllegalArgumentException | ParseException e) {
      e.printStackTrace();
      response =  Response.serverError().build();
    } finally {
      return response;
    }
  }

  private Token generateToken(String username, List<String> audience, Integer userId, String issuer, String privateKeyString, Integer expiresIn) {
    try {
      /**
       * Generate uuid for token ID
       */
      MessageDigest cryptoProvider = MessageDigest.getInstance("SHA-256");
      UUID randomUuid = UUID.randomUUID();
      cryptoProvider.update(randomUuid.toString().getBytes());
      String tokenId = Base64.getUrlEncoder().encodeToString(cryptoProvider.digest());
      /**
       * Create token with JWT lib
       * TODO решить как задавать audience, возможно конкретное приложение, и пересоздавать токен
       * TODO пересмотреть концепцию передачи данных пользователя
       */
      Token token = new TokenImpl(tokenId, audience != null ? audience : Collections.singletonList("RFInfo"),username + ":" + userId,
        issuer, addHours(new Date(), expiresIn));
      /**
       * Sign token with private key
       */
      Signer signer = new SignerRSA(privateKeyString);
      token.sign(signer);
      return token;
    } catch (Throwable th) {
      th.printStackTrace();
      throw new RuntimeException(th);
    }
  }

  private Date addHours(Date date, int hours) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.HOUR_OF_DAY, hours);
    return calendar.getTime();
  }
}

package org.jepria.oauth.token;

import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDto;
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

import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TokenService {

  private AuthRequestDto getAuthRequest(String authCode, String clientCode, String redirectUri, String tokenId)  {
    AuthRequestSearchDto searchTemplate = new AuthRequestSearchDto();
    searchTemplate.setAuthorizationCode(authCode);
    searchTemplate.setClientCode(clientCode);
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


  private ClientDto getClient(String clientCode) throws IllegalArgumentException {
    ClientSearchDto clientSearchTemplate = new ClientSearchDto();
    clientSearchTemplate.setClientCode(clientCode);
    List<ClientDto> result = (List<ClientDto>) ClientServerFactory.getInstance().getDao().find(clientSearchTemplate, 1);
    if (result.size() == 1) {
      return result.get(0);
    } else {
      throw new NoSuchElementException("Client not found");
    }
  }

  private Date addHours(Date date, int hours) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.add(Calendar.HOUR_OF_DAY, hours);
    return calendar.getTime();
  }

  public TokenDto createTokenByGrantCode(String privateKeyString, String hostContext, String authCode, String clientCode, String clientSecret, String redirectUri) {
    if (authCode == null) {
      throw new IllegalArgumentException("Authorization code is null.");
    }
    if (clientCode == null) {
      throw new IllegalArgumentException("Client ID is null.");
    }
    if (clientSecret == null) {
      throw new IllegalArgumentException("Client Secret is null.");
    }
    if (redirectUri == null) {
      throw new IllegalArgumentException("Redirect URI is null.");
    }
    AuthRequestDto authRequest = getAuthRequest(authCode, clientCode, redirectUri, null);
    if (TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - authRequest.getDateIns().getTime()) > 10) {
      throw new IllegalStateException("Authorization code active time has expired.");
    }
    if (authRequest.getTokenId() != null) {
      throw new IllegalStateException("Request is finished.");
    }
    ClientDto client = getClient(clientCode);
    if (!client.getClientSecret().equals(clientSecret)) {
      throw new IllegalArgumentException("Wrong client secret");
    }
    Token token = generateToken(authRequest.getOperatorLogin(), authRequest.getOperator().getValue(), hostContext, privateKeyString);
    TokenDto tokenDto = new TokenDto();
    tokenDto.setAccess_token(token.asString());
    tokenDto.setExpires_in(14400); // 4 hours active time
    tokenDto.setToken_type("Bearer");
    updateAuthRequest(authRequest.getAuthRequestId(), authRequest.getOperator().getValue(), token.getJti(), new Date());
    return tokenDto;
  }

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

  public Response revokeToken(String clientId, String tokenString, String redirectUri) {
    if (redirectUri != null) {
      try {
        URI redirectionURI = URI.create(new String(Base64.getUrlDecoder().decode(redirectUri)));
        return Response.temporaryRedirect(redirectionURI).build();
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

  public Response create(String grantType, String privateKey, String host, String authCode, String clientCode, String clientSecret, String redirectUri) {
    Response response = null;
    try {
      switch (grantType) {
        case GrantType.AUTHORIZATION_CODE: {
          TokenDto result = new TokenService().createTokenByGrantCode(privateKey, host, authCode, clientCode, clientSecret, new String(Base64.getUrlDecoder().decode(redirectUri)));
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

  public Token generateToken(String operatorLogin, Integer operatorId, String issuer, String privateKeyString) {
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
       */
      Token token = new TokenImpl(tokenId, Collections.singletonList("RFInfo"),operatorLogin + ":" + operatorId,
        issuer, addHours(new Date(), 4));
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
}

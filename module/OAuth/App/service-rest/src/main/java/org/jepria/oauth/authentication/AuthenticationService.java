package org.jepria.oauth.authentication;

import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDtoLocal;
import org.jepria.oauth.authorization.dto.AuthRequestUpdateDto;
import org.jepria.oauth.sdk.GrantType;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.oauth.token.dto.TokenDto;

import javax.security.auth.login.LoginException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.jepria.oauth.authorization.AuthorizationFieldNames.AUTH_REQUEST_ID;
import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthenticationService {

  public Integer loginByPassword(String username, String password) throws LoginException {
    Integer operatorId = AuthenticationServerFactory.getInstance().getDao().loginByPassword(username, password);
    if (operatorId == null) {
      throw new LoginException();
    } else {
      return operatorId;
    }
  }

  private AuthRequestDto getAuthRequest(String authCode, String clientId, String redirectUri) {
    try {
      AuthRequestSearchDtoLocal searchTemplate = new AuthRequestSearchDtoLocal();
      searchTemplate.setClientId(clientId);
      searchTemplate.setAuthorizationCode(authCode);
      searchTemplate.setRedirectUri(redirectUri);
      AuthRequestDto authRequest = AuthorizationServerFactory.getInstance().getService().find(searchTemplate).get(0);
      return authRequest;
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  public Integer authenticate(
    String authCode,
    String redirectUri,
    String clientId,
    String username,
    String password) throws LoginException {
      AuthRequestDto authRequest = getAuthRequest(authCode, clientId, redirectUri);
      if (authRequest == null || TimeUnit.MILLISECONDS.toMinutes(new Date().getTime() - authRequest.getDateIns().getTime()) > 10) {
        throw new IllegalStateException("Authorization code not found or has expired");
      }
      if (authRequest.getOperator().getValue() != null) {
        throw new IllegalArgumentException("Request has already passed authentication");
      }
      if (authRequest.getTokenId() != null) {
        throw new IllegalArgumentException("Request is finished");
      }
      Integer operatorId = loginByPassword(username, password);
      setOperatorId(authRequest.getAuthRequestId(), operatorId);
      return operatorId;
  }

  public TokenDto getToken(String privateKey, String host, String authCode, String clientId, String redirectUri) {
    return TokenServerFactory.getInstance().getService().createTokenForImplicitGrant(privateKey, host, authCode, clientId, redirectUri);
  }

  private void setOperatorId(Integer authRequestId, Integer operatorId) {
    AuthRequestUpdateDto updateDto = new AuthRequestUpdateDto();
    updateDto.setOperatorId(operatorId);
    AuthorizationServerFactory.getInstance().getDao().update(new HashMap<String, Integer>(){{
      put(AUTH_REQUEST_ID, authRequestId);
    }}, updateDto, 1);
  }
}

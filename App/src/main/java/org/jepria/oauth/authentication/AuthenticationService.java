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

  private AuthRequestDto getAuthRequest(String authCode, String redirectUri) {
    try {
      AuthRequestSearchDtoLocal searchTemplate = new AuthRequestSearchDtoLocal();
      searchTemplate.setAuthorizationCode(authCode);
      searchTemplate.setRedirectUri(redirectUri);
      AuthRequestDto authRequest = AuthorizationServerFactory.getInstance().getService().find(searchTemplate).get(0);
      return authRequest;
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  public Response authenticate(
    String privateKey,
    String host,
    String responseType,
    String authCode,
    String state,
    String redirectUriEncoded,
    String clientId,
    String clientName,
    String username,
    String password) {
    String redirectUri = new String(Base64.getUrlDecoder().decode(redirectUriEncoded));
    Response response = null;
    try {
      AuthRequestDto authRequest = getAuthRequest(authCode, redirectUri);
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
      if (CODE.equalsIgnoreCase(responseType)) {
        response = Response.status(302).location(URI.create(redirectUri + getSeparator(redirectUri) + CODE + "=" + authCode + "&" + (state != null ? STATE + "=" + state : ""))).build();
      } else if (TOKEN.equalsIgnoreCase(responseType)) {
        TokenDto tokenDto = TokenServerFactory.getInstance().getService().createForImplicitGrant(privateKey, host, authCode, clientId, redirectUri);
        response = Response.status(302).location(URI.create(redirectUri
          + "#" + "access_token=" + tokenDto.getAccessToken()
          + "&token_type=" + tokenDto.getTokenType()
          + "&expires_in=" + tokenDto.getExpiresIn()
          + "&" + STATE + "=" + state)).build();
      } else {
        response =  Response.status(302).location(URI.create(redirectUri + getSeparator(redirectUri) + ERROR_QUERY_PARAM + UNSUPPORTED_RESPONSE_TYPE)).build();
      }
    } catch (IllegalStateException | IllegalArgumentException e) {
      e.printStackTrace();
      response =  Response.status(302).location(URI.create(redirectUri + getSeparator(redirectUri) + ERROR_QUERY_PARAM + ACCESS_DENIED + "&" + ERROR_DESCRIPTION_QUERY_PARAM + URLEncoder.encode(e.getMessage(), "UTF-8"))).entity(null).build();
    } catch (LoginException e) {
      e.printStackTrace();
      response =  Response.status(302).location(new URI("/oauth/login/?" + RESPONSE_TYPE + "=" + CODE + "&" + CODE + "=" + authCode
        + "&" + REDIRECT_URI + "=" + redirectUriEncoded + "&" + CLIENT_NAME + "=" + clientName + "&" + STATE + "=" + state + "&error-code=" + 401)).build();
    } catch (Exception e) {
      e.printStackTrace();
      response =  Response.status(302).location(URI.create(redirectUri + getSeparator(redirectUri) + ERROR_QUERY_PARAM + "&" + SERVER_ERROR)).build();
    } finally {
      return response;
    }
  }

  private void setOperatorId(Integer authRequestId, Integer operatorId) {
    AuthRequestUpdateDto updateDto = new AuthRequestUpdateDto();
    updateDto.setOperatorId(operatorId);
    AuthorizationServerFactory.getInstance().getDao().update(new HashMap<String, Integer>(){{
      put(AUTH_REQUEST_ID, authRequestId);
    }}, updateDto, 1);
  }

  /**
   * Get next separator for URI
   *
   * @param uri
   * @return
   */
  public static String getSeparator(String uri) {
    String separator = "";
    if (uri != null) {
      if (uri.contains("?")) {
        separator = "&";
      } else if (uri.endsWith("/")) {
        separator = "?";
      } else {
        separator = "/?";
      }
    }
    return separator;
  }
}

package org.jepria.oauth.authorization.rest;

import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.exception.OAuthRuntimeException;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthorizationJaxrsAdapter extends JaxrsAdapterBase {
  @Inject
  AuthorizationServerFactory authorizationServerFactory;
  @Inject
  TokenServerFactory tokenServerFactory;
  @Context
  HttpServletRequest request;

  private String getHostContext() {
    return URI.create(request.getRequestURL().toString()).resolve(request.getContextPath()).toString();
  }

  @GET
  @Path("/authorize")
  @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.TEXT_HTML, "text/x-gwt-rpc"})
// TODO delete after getting off GWT (cause GWT send redirect query with 'content-type: text/x-gwt-rpc' in IE
  public Response authorize(@QueryParam("response_type") String responseType,
                            @QueryParam("client_id") String clientId,
                            @QueryParam("redirect_uri") String redirectUriEncoded,
                            @QueryParam("code_challenge") String codeChallenge,
                            @QueryParam("state") String state,
                            @CookieParam(SESSION_ID) String sessionToken) {
    String redirectUri = null;
    if (redirectUriEncoded == null) {
      throw new OAuthRuntimeException(INVALID_REQUEST, "redirect_uri is null");
    }
    try {
      redirectUri = URLDecoder.decode(redirectUriEncoded.replaceAll("%20", "\\+"), StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (!isValidUri(redirectUri)) {
      throw new OAuthRuntimeException(INVALID_REQUEST, "redirect_uri is invalid");
    }
    Response response = null;
    SessionDto session;

    if (sessionToken != null) {
      session = authorizationServerFactory
          .getService()
          .authorize(responseType,
              clientId,
              redirectUri,
              codeChallenge,
              sessionToken,
              getHostContext());
      if (session.getSessionTokenId() != null && new Date().before(session.getSessionTokenDateFinish()) && session.getOperator() != null) {
        if (ResponseType.CODE.equals(responseType)) {
          response = Response
              .status(302)
              .location(URI.create(redirectUri + getSeparator(redirectUri) + CODE + "=" + session.getAuthorizationCode() + (state != null ? "&" + STATE + "=" + state : "")))
              .build();
        } else {
          TokenDto tokenDto = tokenServerFactory.getService().create(responseType, clientId, getHostContext(), session.getAuthorizationCode(), URI.create(redirectUri), 8);
          response = Response.status(302).location(URI.create(redirectUri
              + "#" + ACCESS_TOKEN_QUERY_PARAM + tokenDto.getAccessToken()
              + "&" + TOKEN_TYPE_QUERY_PARAM + tokenDto.getTokenType()
              + "&" + EXPIRES_IN_QUERY_PARAM + tokenDto.getExpiresIn()
              + "&" + STATE + "=" + state))
              .cookie(new NewCookie(SESSION_ID,
                  sessionToken,
                  null,
                  null,
                  null,
                  NewCookie.DEFAULT_MAX_AGE,
                  false,
                  true))
              .build();
        }
      } else {
        try {
          response = Response.status(302).location(URI.create("/oauth/login/?"
              + RESPONSE_TYPE + "=" + responseType
              + "&" + CODE + "=" + session.getAuthorizationCode()
              + "&" + REDIRECT_URI + "=" + redirectUriEncoded
              + "&" + CLIENT_ID + "=" + session.getClient().getValue()
              + "&" + CLIENT_NAME + "=" + URLEncoder.encode(session.getClient().getName(), StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20")
              + "&" + STATE + "=" + state)).build();
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
    } else {
      session = authorizationServerFactory
          .getService()
          .authorize(responseType,
              clientId,
              redirectUri,
              codeChallenge);
      try {
        response = Response.status(302).location(URI.create("/oauth/login/?"
            + RESPONSE_TYPE + "=" + responseType
            + "&" + CODE + "=" + session.getAuthorizationCode()
            + "&" + REDIRECT_URI + "=" + redirectUriEncoded
            + "&" + CLIENT_ID + "=" + session.getClient().getValue()
            + "&" + CLIENT_NAME + "=" + URLEncoder.encode(session.getClient().getName(), StandardCharsets.UTF_8.name()).replaceAll("\\+","%20")
            + "&" + STATE + "=" + state)).build();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
    return response;
  }

  /**
   * Get next separator for URI
   *
   * @param uri
   * @return
   */
  private static String getSeparator(String uri) {
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

  /**
   * @param redirectUri
   * @return
   */
  private boolean isValidUri(String redirectUri) {
    if (redirectUri == null) {
      return false;
    }
    try {
      new URI(redirectUri);
      return true;
    } catch (URISyntaxException | NullPointerException e) {
      e.printStackTrace();
      return false;
    }
  }
}

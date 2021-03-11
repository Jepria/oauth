package org.jepria.oauth.session.rest;

import org.jepria.oauth.authentication.dto.SessionTokenDto;
import org.jepria.oauth.main.security.ClientCredentials;
import org.jepria.oauth.session.LoginConfirmService;
import org.jepria.oauth.session.SessionServerFactory;
import org.jepria.oauth.session.dto.LoginConfirmDto;
import org.jepria.oauth.token.TokenServerFactory;
import org.jepria.oauth.token.TokenService;
import org.jepria.oauth.token.dto.TokenDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.jepria.oauth.main.Utils.*;
import static org.jepria.oauth.sdk.OAuthConstants.*;

@Path("/session")
public class SessionJaxrsAdapter extends JaxrsAdapterBase {
  
  protected final LoginConfirmService service;
  protected final TokenService tokenService;
  
  @Inject
  public SessionJaxrsAdapter(SessionServerFactory serverFactory, TokenServerFactory tokenServerFactory) {
    this.service = serverFactory.getService();
    this.tokenService = tokenServerFactory.getService();
  }
  
  @PUT
  @Path("/{sessionId}")
  @ClientCredentials
  @RolesAllowed("OALoginModule")
  public Response updateSession(@NotBlank @PathParam("sessionId") String sessionId, @Valid LoginConfirmDto dto) throws UnsupportedEncodingException {
    SessionTokenDto sessionToken = service.confirm(sessionId, dto, getHostContextPath(request), securityContext.getCredential(), getSessionTokenLifeTime(request));
    Response response;
    if (CODE.equalsIgnoreCase(dto.getResponseType())) {
      response = Response.status(302)
          .location(URI.create(dto.getRedirectUri() +
              getSeparator(dto.getRedirectUri()) +
              CODE + "=" + sessionToken.getAuthorizationCode() +
              "&" + (dto.getState() != null ? dto.getState() + "=" + dto.getState() : "")))
          .cookie(new NewCookie(SESSION_ID,
              sessionToken.getToken(),
              null,
              null,
              1,
              null,
              -1,
              sessionToken.getExpirationDate(),
              false,
              true))
          .build();
    } else if (TOKEN.equalsIgnoreCase(dto.getResponseType())) {
      TokenDto tokenDto = tokenService.create(dto.getRedirectUri(),
          getHostContextPath(request),
          sessionToken.getAuthorizationCode(),
          dto.getClientId(),
          URI.create(dto.getRedirectUri()),
          getAccessTokenLifeTime(request));
      response = Response.status(302).location(URI.create(dto.getRedirectUri()
          + "#" + ACCESS_TOKEN_QUERY_PARAM + tokenDto.getAccessToken()
          + "&" + TOKEN_TYPE_QUERY_PARAM + tokenDto.getTokenType()
          + "&" + EXPIRES_IN_QUERY_PARAM + tokenDto.getExpiresIn()
          + "&" + dto.getState() + "=" + URLEncoder.encode(dto.getState(), StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20")))
          .cookie(new NewCookie(SESSION_ID,
              sessionToken.getToken(),
              null,
              null,
              1,
              null,
              -1,
              sessionToken.getExpirationDate(),
              false,
              true))
          .build();
    } else {
      response = Response.status(302).location(URI.create(dto.getRedirectUri() + getSeparator(dto.getRedirectUri()) + ERROR_QUERY_PARAM + UNSUPPORTED_RESPONSE_TYPE)).build();
    }
    return response;
  }
  
}

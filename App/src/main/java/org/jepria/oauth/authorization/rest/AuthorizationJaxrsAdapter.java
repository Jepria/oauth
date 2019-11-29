package org.jepria.oauth.authorization.rest;

import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.authorization.AuthorizationService;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.main.dto.ErrorDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import static org.jepria.oauth.sdk.OAuthConstants.CODE;
import static org.jepria.oauth.sdk.OAuthConstants.TOKEN;

/**
 * The authorization code grant type is used to obtain both access
 *    tokens and optionally refresh tokens and is optimized for confidential clients.
 *    Since this is a redirection-based flow, the client must be capable of
 *    interacting with the resource owner's user-agent (typically a web
 *    browser) and capable of receiving incoming requests (via redirection)
 *    from the authorization server.
 */
public class AuthorizationJaxrsAdapter extends JaxrsAdapterBase {
  @Context
  HttpServletRequest request;

  @GET
  @Path("/authorize")
  @Consumes({MediaType.TEXT_HTML, "text/x-gwt-rpc"})// TODO delete after getting off GWT (cause GWT send redirect query with 'content-type: text/x-gwt-rpc' in IE
  public Response authorize(@QueryParam("response_type") String responseType,
                            @QueryParam("client_id") String clientId,
                            @QueryParam("redirect_uri") String redirectUri,
                            @QueryParam("state") String state) {
    return AuthorizationServerFactory.getInstance().getService().authorize(responseType, clientId, redirectUri, state, request.getServletContext().getContextPath());
  }
}

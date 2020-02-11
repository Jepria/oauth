package org.jepria.oauth.authorization.rest;

import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDtoLocal;
import org.jepria.oauth.main.exception.HandledRuntimeException;
import org.jepria.oauth.sdk.ResponseType;
import org.jepria.oauth.sdk.util.URIUtil;
import org.jepria.server.data.SearchRequestDto;
import org.jepria.server.service.rest.ErrorDto;
import org.jepria.server.service.rest.ExtendedResponse;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.rest.jersey.ExceptionManager;
import org.jepria.server.service.rest.jersey.ExceptionManagerImpl;
import org.jepria.server.service.security.HttpBasic;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import static org.jepria.oauth.sdk.OAuthConstants.*;

public class AuthorizationJaxrsAdapter extends JaxrsAdapterBase {
  @Context
  HttpServletRequest request;
  ExceptionManager exceptionManager = new ExceptionManagerImpl();

  private String getPublicKey() {
    return request.getServletContext().getInitParameter("org.jepria.auth.jwt.PublicKey");
  }

  private String getPrivateKey() {
    return request.getServletContext().getInitParameter("org.jepria.auth.jwt.PrivateKey");
  }

  private String getHostContext() {
    return URI.create(request.getRequestURL().toString()).resolve(request.getContextPath()).toString();
  }

  protected final EntityEndpointAdapter entityEndpointAdapter = new EntityEndpointAdapter(() -> AuthorizationServerFactory.getInstance().getEntityService());

  protected final SearchEndpointAdapter searchEndpointAdapter = new SearchEndpointAdapter(() -> AuthorizationServerFactory.getInstance().getSearchService(() -> request.getSession()));

  @GET
  @Path("/authorize")
  @Consumes({MediaType.TEXT_HTML, "text/x-gwt-rpc"})
// TODO delete after getting off GWT (cause GWT send redirect query with 'content-type: text/x-gwt-rpc' in IE
  public Response authorize(@QueryParam("response_type") String responseType,
                            @QueryParam("client_id") String clientId,
                            @QueryParam("redirect_uri") String redirectUriEncoded,
                            @QueryParam("code_challenge") String codeChallenge,
                            @QueryParam("state") String state,
                            @CookieParam(SESSION_ID) String sessionToken) {
    String redirectUri;
    try {
      redirectUri = new String(Base64.getUrlDecoder().decode(redirectUriEncoded));
      if (!isValidUri(redirectUri)) {
        ErrorDto errorDto = exceptionManager.registerExceptionAndPrepareErrorDto(new IllegalArgumentException("redirect_uri is invalid"));
        return Response.status(Response.Status.BAD_REQUEST).entity(errorDto).build();
      }
    } catch (Throwable th) {
      throw new HandledRuntimeException(INVALID_REQUEST, "redirect_uri is null or invalid");
    }
    Response response = null;
    AuthRequestDto authRequest;
    if (sessionToken != null) {
      authRequest = AuthorizationServerFactory.getInstance().getService().authorize(responseType, clientId, redirectUri, codeChallenge, sessionToken, getHostContext(), getPublicKey(), getPrivateKey());
      if (authRequest.getOperator() != null) {
        response = Response
          .status(302)
          .location(URI.create(redirectUri + getSeparator(redirectUri) + CODE + "=" + authRequest.getAuthorizationCode() + "&" + (state != null ? STATE + "=" + state : "")))
          .build();
      } else {
        response = Response.status(302).location(URI.create("/oauth/login/?"
          + RESPONSE_TYPE + "=" + CODE
          + "&" + CODE + "=" + authRequest.getAuthorizationCode()
          + "&" + REDIRECT_URI + "=" + redirectUriEncoded
          + "&" + CLIENT_ID + "=" + authRequest.getClient().getValue()
          + "&" + CLIENT_NAME + "=" + authRequest.getClient().getName()
          + "&" + STATE + "=" + state)).build();
      }
    } else {
      authRequest = AuthorizationServerFactory.getInstance().getService().authorize(responseType, clientId, redirectUri, codeChallenge);
      response = Response.status(302).location(URI.create("/oauth/login/?"
        + RESPONSE_TYPE + "=" + CODE
        + "&" + CODE + "=" + authRequest.getAuthorizationCode()
        + "&" + REDIRECT_URI + "=" + redirectUriEncoded
        + "&" + CLIENT_ID + "=" + authRequest.getClient().getValue()
        + "&" + CLIENT_NAME + "=" + authRequest.getClient().getName()
        + "&" + STATE + "=" + state)).build();
    }
    return response;
  }

  @GET
  @Path("/logout")
  public Response logout(
    @QueryParam("client_id") String clientId,
    @QueryParam("redirect_uri") String redirectUriEncoded,
    @QueryParam("state") String state,
    @CookieParam(SESSION_ID) String sessionToken
  ) {
    String redirectUri = new String(Base64.getUrlDecoder().decode(redirectUriEncoded));
    AuthorizationServerFactory
      .getInstance()
      .getService()
      .logout(clientId,
        redirectUri,
        sessionToken,
        getHostContext(),
        getPublicKey(),
        getPrivateKey());
    return Response.status(302)
      .location(URI.create(redirectUri + getSeparator(redirectUri) + STATE + "=" + state))
      .cookie(new NewCookie(SESSION_ID, "", null, null, NewCookie.DEFAULT_VERSION, null, 0, new Date(), false, true))
      .build();
  }

  //------------ entity methods ------------//

  @POST
  @Path("/authorization-request/{authRequestId}/block")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response block(@PathParam("authRequestId") Integer authRequestId) {
    AuthorizationServerFactory.getInstance().getService().block(authRequestId);
    return Response.ok().build();
  }

  @GET
  @Path("/authorization-request/{authRequestId}")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response getRecordById(@PathParam("authRequestId") Integer authRequestId) {
    AuthRequestDto result = (AuthRequestDto) entityEndpointAdapter.getRecordById(String.valueOf(authRequestId));
    return Response.ok(result).build();
  }

  //------------ search methods ------------//

  @POST
  @Path("/authorization-request/search")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response postSearch(SearchRequestDto<AuthRequestSearchDto> searchRequestDto,
                             @HeaderParam(ExtendedResponse.REQUEST_HEADER_NAME) String extendedResponse,
                             @HeaderParam("Cache-Control") String cacheControl) {
    AuthRequestSearchDtoLocal searchRequestDtoLocal = new AuthRequestSearchDtoLocal();
    searchRequestDtoLocal.setHasToken(true);
    searchRequestDtoLocal.setAuthRequestId(searchRequestDto.getTemplate().getAuthRequestId());
    searchRequestDtoLocal.setOperatorId(searchRequestDto.getTemplate().getOperatorId());
    searchRequestDtoLocal.setBlocked(searchRequestDto.getTemplate().getBlocked());
    searchRequestDtoLocal.setClientId(searchRequestDto.getTemplate().getClientId());

    SearchRequestDto<AuthRequestSearchDtoLocal> searchTemplate = new SearchRequestDto<>();
    searchTemplate.setTemplate(searchRequestDtoLocal);
    return searchEndpointAdapter.postSearch(searchTemplate, extendedResponse, cacheControl);
  }

  @GET
  @Path("/authorization-request/search/{searchId}")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response getSearchRequest(
    @PathParam("searchId") String searchId) {
    SearchRequestDto<AuthRequestSearchDtoLocal> result = (SearchRequestDto<AuthRequestSearchDtoLocal>) searchEndpointAdapter.getSearchRequest(searchId);

    AuthRequestSearchDto searchRequestDto = new AuthRequestSearchDto();
    searchRequestDto.setAuthRequestId(result.getTemplate().getAuthRequestId());
    searchRequestDto.setOperatorId(result.getTemplate().getOperatorId());
    searchRequestDto.setBlocked(result.getTemplate().getBlocked());
    searchRequestDto.setClientId(result.getTemplate().getClientId());

    SearchRequestDto<AuthRequestSearchDto> searchTemplate = new SearchRequestDto<>();
    searchTemplate.setTemplate(searchRequestDto);
    return Response.ok(searchTemplate).build();
  }

  @GET
  @Path("/authorization-request/search/{searchId}/resultset-size")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response getSearchResultsetSize(@PathParam("searchId") String searchId,
                                         @HeaderParam("Cache-Control") String cacheControl) {
    int result = searchEndpointAdapter.getSearchResultsetSize(searchId, cacheControl);
    return Response.ok(result).build();
  }

  @GET
  @Path("/authorization-request/search/{searchId}/resultset")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response getResultset(
    @PathParam("searchId") String searchId,
    @QueryParam("pageSize") Integer pageSize,
    @QueryParam("page") Integer page,
    @HeaderParam("Cache-Control") String cacheControl) {
    List<AuthRequestDto> result = (List<AuthRequestDto>) searchEndpointAdapter.getResultset(searchId, pageSize, page, cacheControl);
    return Response.ok(result).build();
  }

  @GET
  @Path("/authorization-request/search/{searchId}/resultset/paged-by-{pageSize:\\d+}/{page}")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response getResultsetPaged(
    @PathParam("searchId") String searchId,
    @PathParam("pageSize") Integer pageSize,
    @PathParam("page") Integer page,
    @HeaderParam("Cache-Control") String cacheControl) {
    List<AuthRequestDto> result = (List<AuthRequestDto>) searchEndpointAdapter.getResultsetPaged(searchId, pageSize, page, cacheControl);
    return Response.ok(result).build();
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

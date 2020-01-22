package org.jepria.oauth.authorization.rest;

import org.jepria.oauth.authorization.AuthorizationServerFactory;
import org.jepria.oauth.authorization.dto.AuthRequestDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDto;
import org.jepria.oauth.authorization.dto.AuthRequestSearchDtoLocal;
import org.jepria.server.data.SearchRequestDto;
import org.jepria.server.service.rest.ExtendedResponse;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.HttpBasic;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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

  protected final EntityEndpointAdapter entityEndpointAdapter = new EntityEndpointAdapter(() -> AuthorizationServerFactory.getInstance().getEntityService());

  protected final SearchEndpointAdapter searchEndpointAdapter = new SearchEndpointAdapter(() -> AuthorizationServerFactory.getInstance().getSearchService(() -> request.getSession()));

  @GET
  @Path("/authorize")
  @Consumes({MediaType.TEXT_HTML, "text/x-gwt-rpc"})// TODO delete after getting off GWT (cause GWT send redirect query with 'content-type: text/x-gwt-rpc' in IE
  public Response authorize(@QueryParam("response_type") String responseType,
                            @QueryParam("client_id") String clientId,
                            @QueryParam("redirect_uri") String redirectUri,
                            @QueryParam("state") String state) {
    return AuthorizationServerFactory.getInstance().getService().authorize(responseType, clientId, redirectUri, state);
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
    SearchRequestDto<AuthRequestSearchDtoLocal> result = (SearchRequestDto<AuthRequestSearchDtoLocal>)searchEndpointAdapter.getSearchRequest(searchId);

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
    List<AuthRequestDto> result = (List<AuthRequestDto>)searchEndpointAdapter.getResultset(searchId, pageSize, page, cacheControl);
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
    List<AuthRequestDto> result = (List<AuthRequestDto>)searchEndpointAdapter.getResultsetPaged(searchId, pageSize, page, cacheControl);
    return Response.ok(result).build();
  }
}

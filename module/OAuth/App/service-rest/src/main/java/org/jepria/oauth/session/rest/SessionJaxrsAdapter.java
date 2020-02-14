package org.jepria.oauth.session.rest;

import org.jepria.oauth.session.SessionServerFactory;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.oauth.session.dto.SessionSearchDtoLocal;
import org.jepria.server.data.SearchRequestDto;
import org.jepria.server.service.rest.ExtendedResponse;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.HttpBasic;
import org.jepria.server.service.security.JepSecurityContext;

import javax.ws.rs.*;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.DELETE;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/session")
public class SessionJaxrsAdapter extends JaxrsAdapterBase {
  @Context
  JepSecurityContext securityContext;

  protected final EntityEndpointAdapter entityEndpointAdapter = new EntityEndpointAdapter(() -> SessionServerFactory.getInstance().getEntityService());

  protected final SearchEndpointAdapter searchEndpointAdapter = new SearchEndpointAdapter(() -> SessionServerFactory.getInstance().getSearchService(() -> request.getSession()));

  //------------ entity methods ------------//

  @DELETE
  @Path("/{sessionId}")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response block(@PathParam("sessionId") Integer sessionId) {
    entityEndpointAdapter.deleteRecordById(String.valueOf(sessionId));
    return Response.ok().build();
  }

  @GET
  @Path("/{sessionId}")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response getRecordById(@PathParam("sessionId") Integer sessionId) {
    SessionDto result = (SessionDto) entityEndpointAdapter.getRecordById(String.valueOf(sessionId));
    return Response.ok(result).build();
  }

  //------------ search methods ------------//

  @POST
  @Path("/search")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response postSearch(SearchRequestDto<SessionSearchDto> searchRequestDto,
                             @HeaderParam(ExtendedResponse.REQUEST_HEADER_NAME) String extendedResponse,
                             @HeaderParam("Cache-Control") String cacheControl) {
    SessionSearchDtoLocal searchRequestDtoLocal = new SessionSearchDtoLocal();
    searchRequestDtoLocal.setHasToken(true);
    searchRequestDtoLocal.setSessionId(searchRequestDto.getTemplate().getSessionId());
    searchRequestDtoLocal.setOperatorId(searchRequestDto.getTemplate().getOperatorId());
    searchRequestDtoLocal.setBlocked(searchRequestDto.getTemplate().getBlocked());
    searchRequestDtoLocal.setClientId(searchRequestDto.getTemplate().getClientId());

    SearchRequestDto<SessionSearchDtoLocal> searchTemplate = new SearchRequestDto<>();
    searchTemplate.setTemplate(searchRequestDtoLocal);
    return searchEndpointAdapter.postSearch(searchTemplate, extendedResponse, cacheControl);
  }

  @GET
  @Path("/search/{searchId}")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response getSearchRequest(
    @PathParam("searchId") String searchId) {
    SearchRequestDto<SessionSearchDtoLocal> result = (SearchRequestDto<SessionSearchDtoLocal>) searchEndpointAdapter.getSearchRequest(searchId);

    SessionSearchDto searchRequestDto = new SessionSearchDto();
    searchRequestDto.setSessionId(result.getTemplate().getSessionId());
    searchRequestDto.setOperatorId(result.getTemplate().getOperatorId());
    searchRequestDto.setBlocked(result.getTemplate().getBlocked());
    searchRequestDto.setClientId(result.getTemplate().getClientId());

    SearchRequestDto<SessionSearchDto> searchTemplate = new SearchRequestDto<>();
    searchTemplate.setTemplate(searchRequestDto);
    return Response.ok(searchTemplate).build();
  }

  @GET
  @Path("/search/{searchId}/resultset-size")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response getSearchResultsetSize(@PathParam("searchId") String searchId,
                                         @HeaderParam("Cache-Control") String cacheControl) {
    int result = searchEndpointAdapter.getSearchResultsetSize(searchId, cacheControl);
    return Response.ok(result).build();
  }

  @GET
  @Path("/search/{searchId}/resultset")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response getResultset(
    @PathParam("searchId") String searchId,
    @QueryParam("pageSize") Integer pageSize,
    @QueryParam("page") Integer page,
    @HeaderParam("Cache-Control") String cacheControl) {
    List<SessionDto> result = (List<SessionDto>) searchEndpointAdapter.getResultset(searchId, pageSize, page, cacheControl);
    return Response.ok(result).build();
  }

  @GET
  @Path("/search/{searchId}/resultset/paged-by-{pageSize:\\d+}/{page}")
  @HttpBasic(passwordType = HttpBasic.PASSWORD)
  public Response getResultsetPaged(
    @PathParam("searchId") String searchId,
    @PathParam("pageSize") Integer pageSize,
    @PathParam("page") Integer page,
    @HeaderParam("Cache-Control") String cacheControl) {
    List<SessionDto> result = (List<SessionDto>) searchEndpointAdapter.getResultsetPaged(searchId, pageSize, page, cacheControl);
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
}

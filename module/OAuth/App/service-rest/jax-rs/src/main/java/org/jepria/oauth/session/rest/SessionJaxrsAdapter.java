package org.jepria.oauth.session.rest;

import org.jepria.oauth.session.SessionServerFactory;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.SearchRequestDto;
import org.jepria.server.service.rest.ExtendedResponse;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.rest.gson.JsonConfig;
import org.jepria.server.service.security.JepSecurityContext;
import org.jepria.server.service.security.oauth.OAuth;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/session")
@OAuth
public class SessionJaxrsAdapter extends JaxrsAdapterBase {

  @Inject
  SessionServerFactory sessionServerFactory;
  @Context
  JepSecurityContext securityContext;

  protected final EntityEndpointAdapter entityEndpointAdapter = new EntityEndpointAdapter(() -> sessionServerFactory.getEntityService());

  protected final SearchEndpointAdapter searchEndpointAdapter = new SearchEndpointAdapter(() -> sessionServerFactory.getSearchService(() -> request.getSession()));

  //------------ entity methods ------------//

  @DELETE
  @Path("/{sessionId}")
  @RolesAllowed("OADeleteSession")
  public Response delete(@PathParam("sessionId") Integer sessionId) {
    entityEndpointAdapter.deleteRecordById(String.valueOf(sessionId));
    return Response.ok().build();
  }

  @DELETE
  @Path("/delete-all/{operatorId}")
  @RolesAllowed("OADeleteSession")
  public Response deleteAll(@PathParam("operatorId") Integer operatorId) {
    sessionServerFactory.getService().deleteAll(operatorId, securityContext.getCredential());
    return Response.ok().build();
  }

  @GET
  @Path("/{sessionId}")
  @RolesAllowed("OAViewSession")
  @JsonConfig(dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  public Response getRecordById(@PathParam("sessionId") Integer sessionId) {
    SessionDto result = (SessionDto) entityEndpointAdapter.getRecordById(String.valueOf(sessionId));
    return Response.ok(result).build();
  }

  //------------ search methods ------------//

  @POST
  @Path("/search")
  @RolesAllowed("OAViewSession")
  public Response postSearch(SearchRequestDto<SessionSearchDto> searchRequestDto,
                             @HeaderParam(ExtendedResponse.REQUEST_HEADER_NAME) String extendedResponse,
                             @HeaderParam("Cache-Control") String cacheControl) {
    return searchEndpointAdapter.postSearch(searchRequestDto, extendedResponse, cacheControl);
  }

  @GET
  @Path("/search/{searchId}")
  @RolesAllowed("OAViewSession")
  public Response getSearchRequest(
    @PathParam("searchId") String searchId) {
    SearchRequestDto<SessionSearchDto> result = (SearchRequestDto<SessionSearchDto>) searchEndpointAdapter.getSearchRequest(searchId);

    SessionSearchDto searchRequestDto = new SessionSearchDto();
    searchRequestDto.setSessionId(result.getTemplate().getSessionId());
    searchRequestDto.setOperatorId(result.getTemplate().getOperatorId());
    searchRequestDto.setClientId(result.getTemplate().getClientId());
    searchRequestDto.setMaxRowCount(result.getTemplate().getMaxRowCount());

    SearchRequestDto<SessionSearchDto> searchTemplate = new SearchRequestDto<>();
    searchTemplate.setTemplate(searchRequestDto);
    return Response.ok(searchTemplate).build();
  }

  @GET
  @Path("/search/{searchId}/resultset-size")
  @RolesAllowed("OAViewSession")
  public Response getSearchResultsetSize(@PathParam("searchId") String searchId,
                                         @HeaderParam("Cache-Control") String cacheControl) {
    int result = searchEndpointAdapter.getSearchResultsetSize(searchId, cacheControl);
    return Response.ok(result).build();
  }

  @GET
  @Path("/search/{searchId}/resultset")
  @RolesAllowed("OAViewSession")
  @JsonConfig(dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  public Response getResultset(
    @PathParam("searchId") String searchId,
    @QueryParam("pageSize") Integer pageSize,
    @QueryParam("page") Integer page,
    @HeaderParam("Cache-Control") String cacheControl) {
    List<SessionDto> result = (List<SessionDto>) searchEndpointAdapter.getResultset(searchId, pageSize, page, cacheControl);
    if (result != null && result.size() > 0) {
      return Response.ok(result).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @GET
  @Path("/search/{searchId}/resultset/paged-by-{pageSize:\\d+}/{page}")
  @RolesAllowed("OAViewSession")
  @JsonConfig(dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  public Response getResultsetPaged(
    @PathParam("searchId") String searchId,
    @PathParam("pageSize") Integer pageSize,
    @PathParam("page") Integer page,
    @HeaderParam("Cache-Control") String cacheControl) {
    List<SessionDto> result = (List<SessionDto>) searchEndpointAdapter.getResultsetPaged(searchId, pageSize, page, cacheControl);
    if (result != null && result.size() > 0) {
      return Response.ok(result).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @GET
  @Path("/operators")
  @RolesAllowed("OAViewSession")
  public Response getOperators(@QueryParam("operatorName") String operatorName, @NotNull @QueryParam("maxRowCount") Integer maxRowCount) {
    List<OptionDto<String>> result = sessionServerFactory.getService().getOperators(operatorName, maxRowCount);
    if (!result.isEmpty()) {
      return Response.ok(result).build();
    } else {
      return Response.noContent().build();
    }
  }
}

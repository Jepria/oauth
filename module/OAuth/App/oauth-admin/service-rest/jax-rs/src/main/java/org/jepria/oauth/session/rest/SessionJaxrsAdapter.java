package org.jepria.oauth.session.rest;

import org.jepria.oauth.session.SessionServerFactory;
import org.jepria.oauth.session.SessionService;
import org.jepria.oauth.session.dto.SessionDto;
import org.jepria.oauth.session.dto.SessionSearchDto;
import org.jepria.server.data.ColumnSortConfigurationDto;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.SearchRequestDto;
import org.jepria.server.data.SearchResultDto;
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
  
  protected final SessionService service;
  protected final EntityEndpointAdapter entityEndpointAdapter;
  protected final SearchEndpointAdapter searchEndpointAdapter;
  
  @Inject
  public SessionJaxrsAdapter(SessionServerFactory sessionServerFactory) {
    this.service = sessionServerFactory.getService();
    this.entityEndpointAdapter = new EntityEndpointAdapter(() -> sessionServerFactory.getEntityService());
    this.searchEndpointAdapter =
      new SearchEndpointAdapter(() -> sessionServerFactory.getSearchService(() -> request.getSession()));
  }
  
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
    service.deleteAll(operatorId, securityContext.getCredential());
    return Response.ok().build();
  }
  
  @GET
  @Path("/{sessionId}")
  @RolesAllowed("OAViewSession")
  @JsonConfig(dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
  public Response getRecordById(@PathParam("sessionId") Integer sessionId) {
    SessionDto result = (SessionDto) entityEndpointAdapter.getRecordById(String.valueOf(sessionId));
    return Response.ok(result).build();
  }
  
  @GET
  @Path("/search")
  @RolesAllowed("OAViewSession")
  public Response search(
    @QueryParam("pageSize") Integer pageSize,
    @QueryParam("page") Integer page,
    @QueryParam("sort") List<ColumnSortConfigurationDto> sortConfiguration,
    @BeanParam SessionSearchDto searchRequestDto,
    @HeaderParam("Cache-Control") String cacheControl
  ) {
    SearchResultDto<SessionDto> result = searchEndpointAdapter.search(pageSize, page, sortConfiguration, searchRequestDto,
      cacheControl);
    return Response.ok(result).build();
  }
  
  @GET
  @Path("/operators")
  @RolesAllowed("OAViewSession")
  public Response getOperators(@QueryParam("operatorName") String operatorName, @NotNull @QueryParam("maxRowCount") Integer maxRowCount) {
    List<OptionDto<String>> result = service.getOperators(operatorName, maxRowCount);
    if (!result.isEmpty()) {
      return Response.ok(result).build();
    } else {
      return Response.noContent().build();
    }
  }
}

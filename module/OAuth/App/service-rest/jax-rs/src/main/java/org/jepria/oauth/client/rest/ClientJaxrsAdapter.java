package org.jepria.oauth.client.rest;

import org.hibernate.validator.constraints.NotEmpty;
import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.client.dto.ClientCreateDto;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.client.dto.ClientUpdateDto;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.SearchRequestDto;
import org.jepria.server.service.rest.ExtendedResponse;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.JepSecurityContext;
import org.jepria.server.service.security.oauth.OAuth;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

@Path("/client")
@OAuth
public class ClientJaxrsAdapter extends JaxrsAdapterBase {
  @Context
  JepSecurityContext securityContext;

  protected final EntityEndpointAdapter entityEndpointAdapter = new EntityEndpointAdapter(() -> ClientServerFactory.getInstance().getEntityService());

  protected final SearchEndpointAdapter searchEndpointAdapter = new SearchEndpointAdapter(() -> ClientServerFactory.getInstance().getSearchService(() -> request.getSession()));

  //------------ application-specific methods ------------//
  @GET
  @Path("/grant-types")
  public Response getGrantType() {
    List<String> result = ClientServerFactory.getInstance().getService().getGrantType();
    return Response.ok(result).build();
  }

  @GET
  @Path("/grant-response-types")
  public Response getResponseType(@NotEmpty @QueryParam("grantTypes") List<String> grantTypeCodes) {
    if (grantTypeCodes.size() == 1 && grantTypeCodes.get(0).contains(",")) {
      grantTypeCodes = Arrays.asList(grantTypeCodes.get(0).split(","));
    } else if (grantTypeCodes.size() == 1 && grantTypeCodes.get(0).contains(";")) {
      grantTypeCodes = Arrays.asList(grantTypeCodes.get(0).split(";"));
    }
    List<String> result = ClientServerFactory.getInstance().getService().getGrantResponseType(grantTypeCodes);
    return Response.ok(result).build();
  }

  @GET
  @Path("/application-types")
  public Response getApplicationType() {
    List<String> result = ClientServerFactory.getInstance().getService().getApplicationTypes();
    return Response.ok(result).build();
  }

  @GET
  @Path("/application-grant-types")
  public Response getApplicationGrantType(@NotEmpty @QueryParam("applicationType") String applicationTypeCode) {
    List<String> result = ClientServerFactory.getInstance().getService().getApplicationGrantTypes(applicationTypeCode);
    return Response.ok(result).build();
  }

  @GET
  @RolesAllowed({"OAViewClient", "OAViewSession"})
  public Response getClients(@QueryParam("clientName") String clientName) {
    List<ClientDto> result = ClientServerFactory.getInstance().getService().getClient(clientName, securityContext.getCredential().getOperatorId());
    if (result.isEmpty()) {
      return Response.noContent().build();
    } else {
      return Response.ok(result).build();
    }
  }

  //------------ entity methods ------------//

  @GET
  @Path("{recordId}")
  @RolesAllowed("OAViewClient")
  public Response getRecordById(@PathParam("recordId") String recordId) {
    ClientDto result = (ClientDto) entityEndpointAdapter.getRecordById(recordId);
    return Response.ok(result).build();
  }

  @POST
  @RolesAllowed("OACreateClient")
  public Response create(@Valid ClientCreateDto record) {
    return entityEndpointAdapter.create(record);
  }

  @DELETE
  @Path("{recordId}")
  @RolesAllowed("OADeleteClient")
  public Response deleteRecordById(@PathParam("recordId") String recordId) {
    entityEndpointAdapter.deleteRecordById(recordId);
    return Response.ok().build();
  }

  @PUT
  @Path("{recordId}")
  @RolesAllowed("OAEditClient")
  public Response update(@Valid @PathParam("recordId") String recordId, @Valid ClientUpdateDto record) {
    entityEndpointAdapter.update(recordId, record);
    return Response.ok().build();
  }

  //------------ search methods ------------//

  @POST
  @Path("search")
  @RolesAllowed("OAViewClient")
  public Response postSearch(SearchRequestDto<ClientSearchDto> searchRequestDto,
                             @HeaderParam(ExtendedResponse.REQUEST_HEADER_NAME) String extendedResponse,
                             @HeaderParam("Cache-Control") String cacheControl) {
    return searchEndpointAdapter.postSearch(searchRequestDto, extendedResponse, cacheControl);
  }

  @GET
  @Path("search/{searchId}")
  @RolesAllowed("OAViewClient")
  public Response getSearchRequest(
    @PathParam("searchId") String searchId) {
    SearchRequestDto<ClientSearchDto> result = (SearchRequestDto<ClientSearchDto>) searchEndpointAdapter.getSearchRequest(searchId);
    return Response.ok(result).build();
  }

  @GET
  @Path("search/{searchId}/resultset-size")
  @RolesAllowed("OAViewClient")
  public Response getSearchResultsetSize(@PathParam("searchId") String searchId,
                                         @HeaderParam("Cache-Control") String cacheControl) {
    int result = searchEndpointAdapter.getSearchResultsetSize(searchId, cacheControl);
    return Response.ok(result).build();
  }

  @GET
  @Path("search/{searchId}/resultset")
  @RolesAllowed("OAViewClient")
  public Response getResultset(
    @PathParam("searchId") String searchId,
    @QueryParam("pageSize") Integer pageSize,
    @QueryParam("page") Integer page,
    @HeaderParam("Cache-Control") String cacheControl) {
    List<ClientDto> result = (List<ClientDto>) searchEndpointAdapter.getResultset(searchId, pageSize, page, cacheControl);
    return Response.ok(result).build();
  }

  @GET
  @Path("search/{searchId}/resultset/paged-by-{pageSize:\\d+}/{page}")
  @RolesAllowed("OAViewClient")
  public Response getResultsetPaged(
    @PathParam("searchId") String searchId,
    @PathParam("pageSize") Integer pageSize,
    @PathParam("page") Integer page,
    @HeaderParam("Cache-Control") String cacheControl) {
    List<ClientDto> result = (List<ClientDto>) searchEndpointAdapter.getResultsetPaged(searchId, pageSize, page, cacheControl);
    return Response.ok(result).build();
  }

  @GET
  @Path("role")
  @RolesAllowed({"OACreateClient", "OAEditClient"})
  public Response getRoles(
    @QueryParam("roleName") String roleName,
    @QueryParam("roleNameEn") String roleNameEn,
    @QueryParam("maxRowCount") Integer maxRowCount
  ) {
    List<OptionDto<String>> result = ClientServerFactory.getInstance().getService().getRoles(roleName, roleNameEn, maxRowCount, securityContext.getCredential().getOperatorId());
    return Response.ok(result).build();
  }
}

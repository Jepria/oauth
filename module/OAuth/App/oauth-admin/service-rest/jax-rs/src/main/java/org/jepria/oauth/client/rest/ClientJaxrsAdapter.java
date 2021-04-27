package org.jepria.oauth.client.rest;

import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.client.ClientService;
import org.jepria.oauth.client.dto.ClientCreateDto;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.client.dto.ClientUpdateDto;
import org.jepria.server.data.ColumnSortConfigurationDto;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.SearchResultDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.JepSecurityContext;
import org.jepria.server.service.security.oauth.OAuth;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

@Path("/client")
@OAuth
public class ClientJaxrsAdapter extends JaxrsAdapterBase {
  
  protected final ClientService service;
  protected final EntityEndpointAdapter entityEndpointAdapter;
  protected final SearchEndpointAdapter searchEndpointAdapter;
  
  @Inject
  public ClientJaxrsAdapter(ClientServerFactory clientServerFactory) {
    this.service = clientServerFactory.getService();
    this.entityEndpointAdapter = new EntityEndpointAdapter(() -> clientServerFactory.getEntityService());
    this.searchEndpointAdapter =
      new SearchEndpointAdapter(() -> clientServerFactory.getSearchService(() -> request.getSession()));
  }

  //------------ application-specific methods ------------//
  @GET
  @Path("/grant-types")
  public Response getGrantType() {
    List<String> result = service.getGrantType();
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
    List<String> result = service.getGrantResponseType(grantTypeCodes);
    return Response.ok(result).build();
  }

  @GET
  @Path("/application-types")
  public Response getApplicationType() {
    List<String> result = service.getApplicationTypes();
    return Response.ok(result).build();
  }

  @GET
  @Path("/application-grant-types")
  public Response getApplicationGrantType(@NotEmpty @QueryParam("applicationType") String applicationTypeCode) {
    List<String> result = service.getApplicationGrantTypes(applicationTypeCode);
    return Response.ok(result).build();
  }

  @GET
  @RolesAllowed({"OAViewClient", "OAViewSession"})
  public Response getClients(@QueryParam("clientName") String clientName) {
    List<ClientDto> result = service.getClient(null, clientName, securityContext.getCredential().getOperatorId());
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
  
  
  @GET
  @Path("/search")
  @RolesAllowed("OAViewClient")
  public Response search(
    @QueryParam("pageSize") Integer pageSize,
    @QueryParam("page") Integer page,
    @QueryParam("sort") List<ColumnSortConfigurationDto> sortConfiguration,
    @BeanParam ClientSearchDto searchRequestDto,
    @HeaderParam("Cache-Control") String cacheControl
  ) {
    SearchResultDto<ClientDto> result = searchEndpointAdapter.search(pageSize, page, sortConfiguration, searchRequestDto,
      cacheControl);
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
    List<OptionDto<String>> result = service.getRoles(roleName, roleNameEn, maxRowCount, securityContext.getCredential().getOperatorId());
    return Response.ok(result).build();
  }
}

package org.jepria.oauth.clienturi.rest;

import org.jepria.oauth.clienturi.ClientUriFieldNames;
import org.jepria.oauth.clienturi.ClientUriServerFactory;
import org.jepria.oauth.clienturi.ClientUriService;
import org.jepria.oauth.clienturi.dto.ClientUriCreateDto;
import org.jepria.oauth.clienturi.dto.ClientUriDto;
import org.jepria.oauth.clienturi.dto.ClientUriSearchDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.oauth.OAuth;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.List;


@Path("/client/{clientId}/client-uri")
@OAuth
public class ClientUriJaxrsAdapter extends JaxrsAdapterBase {
  
  protected final ClientUriService service;
  protected final EntityEndpointAdapter entityEndpointAdapter;
  
  @Inject
  public ClientUriJaxrsAdapter(ClientUriServerFactory clientUriServerFactory) {
    this.service = clientUriServerFactory.getService();
    this.entityEndpointAdapter = new EntityEndpointAdapter(() -> clientUriServerFactory.getEntityService());
  }

  @GET
  @RolesAllowed("OAViewClient")
  public Response getAllRecords(@PathParam("clientId") String clientId){
    ClientUriSearchDto dto = new ClientUriSearchDto();
    dto.setClientId(clientId);
    List<ClientUriDto> result = service.findClientUri(dto, securityContext.getCredential().getOperatorId());
    if (result.size() > 0) {
      return Response.ok().entity(result).build();
    } else {
      return Response.noContent().build();
    }
  }

  ////////// Basic methods (boilerplate) //////////

  @GET
  @Path("{clientUriId}")
  @RolesAllowed("OAViewClient")
  public Response getRecordById(@PathParam("clientId") String clientId, @PathParam("clientUriId") Integer clientUriId) {
    String complexKey = ClientUriFieldNames.CLIENT_ID + "=" + clientId + "~" + ClientUriFieldNames.CLIENT_URI_ID + "=" + clientUriId;
    ClientUriDto result = (ClientUriDto) entityEndpointAdapter.getRecordById(complexKey);
    return Response.ok().entity(result).build();
  }

  /**
   * URI вынесен в Dto, потому что в IE максимальная длина урла 2048. Значение может не поместиться.
   *
   * @param clientId Client app ID
   * @param record {@link ClientUriCreateDto}
   * @return
   */
  @POST
  @RolesAllowed({"OAEditClient", "OACreateClient"})
  public Response create(@PathParam("clientId") String clientId, @Valid ClientUriCreateDto record) {
    record.setClientId(clientId);
    return entityEndpointAdapter.create(record);
  }

  @DELETE
  @Path("{clientUriId}")
  @RolesAllowed({"OAEditClient", "OACreateClient"})
  public Response deleteRecordById(@PathParam("clientId") String clientId, @PathParam("clientUriId") Integer clientUriId) {
    String complexKey = ClientUriFieldNames.CLIENT_ID + "=" + clientId + "~" + ClientUriFieldNames.CLIENT_URI_ID + "=" + clientUriId;
    entityEndpointAdapter.deleteRecordById(complexKey);
    return Response.ok().build();
  }
}

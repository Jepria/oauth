package org.jepria.oauth.client.rest;

import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.client.dto.ClientCreateDto;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.client.dto.ClientUpdateDto;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.SearchRequestDto;
import org.jepria.server.service.rest.ExtendedResponse;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.HttpBasic;
import org.jepria.server.service.security.JepSecurityContext;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.*;

@Path("/client")
@HttpBasic(passwordType = HttpBasic.PASSWORD)
public class ClientJaxrsAdapter extends JaxrsAdapterBase {
  @Context
  JepSecurityContext securityContext;

  protected final EntityEndpointAdapter entityEndpointAdapter = new EntityEndpointAdapter(() -> ClientServerFactory.getInstance().getEntityService());

  protected final SearchEndpointAdapter searchEndpointAdapter = new SearchEndpointAdapter(() -> ClientServerFactory.getInstance().getSearchService(() -> request.getSession()));

  //------------ application-specific methods ------------//
  @GET
  @Path("/option/grant-type")
  public Response getGrantType() {
    List<OptionDto<String>> result = ClientServerFactory.getInstance().getService().getGrantType();
    return Response.ok(result).build();
  }

  @GET
  @Path("/option/grant-response-type")
  public Response getResponseType(@QueryParam("grantTypes") List<String> grantTypeCodes) {
    if (grantTypeCodes.size() == 1 && grantTypeCodes.get(0).contains(",")) {
      grantTypeCodes = Arrays.asList(grantTypeCodes.get(0).split(","));
    } else if (grantTypeCodes.size() == 1 && grantTypeCodes.get(0).contains(";")) {
      grantTypeCodes = Arrays.asList(grantTypeCodes.get(0).split(";"));
    }
    List<OptionDto<String>> result = ClientServerFactory.getInstance().getService().getGrantResponseType(grantTypeCodes);
    return Response.ok(result).build();
  }

  //------------ entity methods ------------//

  @GET
  @Path("{recordId}")
  public Response getRecordById(@PathParam("recordId") String recordId) {
    ClientDto result = (ClientDto) entityEndpointAdapter.getRecordById(recordId);
    return Response.ok(result).build();
  }

  @POST
  public Response create(ClientCreateDto record) {
    try {
      String randomUuid = UUID.randomUUID().toString().replaceAll("-", "");
      record.setClientId(randomUuid);
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] salt = new byte[16];
      random.nextBytes(salt);
      md.update(salt);
      byte[] secret = new byte[32];
      random.nextBytes(secret);
      record.setClientSecret(DatatypeConverter.printHexBinary(md.digest(secret)));
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    return entityEndpointAdapter.create(record);
   // return ClientServerFactory.getInstance().getService().create(request.getRequestURL().toString(), record, securityContext.getCredential());
  }

  @DELETE
  @Path("{recordId}")
  public Response deleteRecordById(@PathParam("recordId") String recordId) {
    entityEndpointAdapter.deleteRecordById(recordId);
    return Response.ok().build();
  }

  @PUT
  @Path("{recordId}")
  public Response update(@PathParam("recordId") String recordId, ClientUpdateDto record) {
    entityEndpointAdapter.update(recordId, record);
    return Response.ok().build();
  }

  //------------ search methods ------------//

  @POST
  @Path("search")
  public Response postSearch(SearchRequestDto<ClientSearchDto> searchRequestDto,
                             @HeaderParam(ExtendedResponse.REQUEST_HEADER_NAME) String extendedResponse,
                             @HeaderParam("Cache-Control") String cacheControl) {
    return searchEndpointAdapter.postSearch(searchRequestDto, extendedResponse, cacheControl);
  }

  @GET
  @Path("search/{searchId}")
  public Response getSearchRequest(
          @PathParam("searchId") String searchId) {
    SearchRequestDto<ClientSearchDto> result = (SearchRequestDto<ClientSearchDto>)searchEndpointAdapter.getSearchRequest(searchId);
    return Response.ok(result).build();
  }

  @GET
  @Path("search/{searchId}/resultset-size")
  public Response getSearchResultsetSize(@PathParam("searchId") String searchId,
                                         @HeaderParam("Cache-Control") String cacheControl) {
    int result = searchEndpointAdapter.getSearchResultsetSize(searchId, cacheControl);
    return Response.ok(result).build();
  }

  @GET
  @Path("search/{searchId}/resultset")
  public Response getResultset(
          @PathParam("searchId") String searchId,
          @QueryParam("pageSize") Integer pageSize,
          @QueryParam("page") Integer page,
          @HeaderParam("Cache-Control") String cacheControl) {
    List<ClientDto> result = (List<ClientDto>)searchEndpointAdapter.getResultset(searchId, pageSize, page, cacheControl);
    return Response.ok(result).build();
  }

  @GET
  @Path("search/{searchId}/resultset/paged-by-{pageSize:\\d+}/{page}")
  public Response getResultsetPaged(
          @PathParam("searchId") String searchId,
          @PathParam("pageSize") Integer pageSize,
          @PathParam("page") Integer page,
          @HeaderParam("Cache-Control") String cacheControl) {
    List<ClientDto> result = (List<ClientDto>)searchEndpointAdapter.getResultsetPaged(searchId, pageSize, page, cacheControl);
    return Response.ok(result).build();
  }
}

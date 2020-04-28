package org.jepria.oauth.client.rest;

import org.hibernate.validator.constraints.NotEmpty;
import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.client.dto.ClientCreateDto;
import org.jepria.oauth.client.dto.ClientDto;
import org.jepria.oauth.client.dto.ClientSearchDto;
import org.jepria.oauth.client.dto.ClientUpdateDto;
import org.jepria.server.data.SearchRequestDto;
import org.jepria.server.service.rest.ExtendedResponse;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.HttpBasic;
import org.jepria.server.service.security.JepSecurityContext;
import org.jepria.server.service.security.OAuth;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Path("/client")
//@HttpBasic(passwordType = HttpBasic.PASSWORD)
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

  //------------ entity methods ------------//

  @GET
  @Path("{recordId}")
  public Response getRecordById(@PathParam("recordId") String recordId) {
    ClientDto result = (ClientDto) entityEndpointAdapter.getRecordById(recordId);
    return Response.ok(result).build();
  }

  @POST
  public Response create(@Valid ClientCreateDto record) {
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
  }

  @DELETE
  @Path("{recordId}")
  public Response deleteRecordById(@PathParam("recordId") String recordId) {
    entityEndpointAdapter.deleteRecordById(recordId);
    return Response.ok().build();
  }

  @PUT
  @Path("{recordId}")
  public Response update(@Valid @PathParam("recordId") String recordId, @Valid ClientUpdateDto record) {
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

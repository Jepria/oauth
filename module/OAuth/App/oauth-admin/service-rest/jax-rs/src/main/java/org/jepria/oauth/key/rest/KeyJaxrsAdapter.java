package org.jepria.oauth.key.rest;

import org.jepria.oauth.key.KeyServerFactory;
import org.jepria.oauth.key.KeyService;
import org.jepria.oauth.key.dto.KeyDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.oauth.OAuth;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Authentication Endpoint takes care of authentication business logic part for Authorization Code Flow and Implicit Flow.
 */
@Path("/key")
@OAuth
public class KeyJaxrsAdapter extends JaxrsAdapterBase {

  protected final KeyService service;
  
  @Inject
  public KeyJaxrsAdapter(KeyServerFactory keyServerFactory) {
    this.service = keyServerFactory.getService();
  }
  
  @GET
  public Response getKey() {
    KeyDto keyDto = service.getKeys(null, securityContext.getCredential());
    if (keyDto != null) {
      keyDto.setPrivateKey("");
      return Response.ok(keyDto).build();
    } else {
      return null;
    }
  }
  
  @POST
  @RolesAllowed("OAUpdateKey")
  public Response updateKeys() {
    service.setKeys(securityContext.getCredential());
    return Response.ok().build();
  }

}

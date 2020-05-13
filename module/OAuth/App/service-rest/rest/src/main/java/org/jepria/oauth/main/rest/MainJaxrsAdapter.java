package org.jepria.oauth.main.rest;

import org.jepria.oauth.main.MainServerFactory;
import org.jepria.server.data.OptionDto;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.OAuth;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

@OAuth
public class MainJaxrsAdapter extends JaxrsAdapterBase {
  
  @GET
  @Path("/operators")
  public Response getOperators(@QueryParam("operatorName") String operatorName, @NotNull @QueryParam("maxRowCount") Integer maxRowCount) {
    List<OptionDto<String>> result = MainServerFactory.getInstance().getService().getOperators(operatorName, maxRowCount);
    if (!result.isEmpty()) {
      return Response.ok(result).build();
    } else {
      return Response.noContent().build();
    }
  }
}

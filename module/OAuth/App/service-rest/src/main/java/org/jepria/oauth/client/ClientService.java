package org.jepria.oauth.client;

import org.jepria.oauth.client.dto.ClientCreateDto;
import org.jepria.oauth.sdk.ApplicationType;
import org.jepria.oauth.sdk.TokenAuthMethod;
import org.jepria.server.data.OptionDto;
import org.jepria.server.service.rest.jersey.validate.ExceptionMapperValidation;
import org.jepria.server.service.security.Credential;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ClientService {

  public List<OptionDto<String>> getGrantType() {
    return ClientServerFactory.getInstance().getDao().getGrantType();
  }

  public List<OptionDto<String>> getGrantResponseType(List<String> grantTypeCodes) {
    return ClientServerFactory.getInstance().getDao().getGrantResponseType(grantTypeCodes);
  }

  public Response create(String requestURL, ClientCreateDto record, Credential credential) {
    if ((ApplicationType.NATIVE.equals(record.getApplicationType())  || ApplicationType.BROWSER.equals(record.getApplicationType())) && !TokenAuthMethod.NONE.equals(record.getTokenAuthMethod())) {
      List<ExceptionMapperValidation.ConstraintViolationDto> violationsDto = new ArrayList<>();
      ExceptionMapperValidation.ConstraintViolationDto violationDtoTokenAuthMethod = new ExceptionMapperValidation.ConstraintViolationDto();
      violationDtoTokenAuthMethod.setPropertyPath("tokenAuthMethod");
      violationDtoTokenAuthMethod.setViolationDescription("Application type and Token Authentication method mismatch");
      violationsDto.add(violationDtoTokenAuthMethod);
      ExceptionMapperValidation.ConstraintViolationDto violationDtoAppType = new ExceptionMapperValidation.ConstraintViolationDto();
      violationDtoAppType.setPropertyPath("applicationType");
      violationDtoAppType.setViolationDescription("Application type and Token Authentication method mismatch");
      violationsDto.add(violationDtoAppType);
      return Response.status(Response.Status.BAD_REQUEST).entity(violationsDto).build();
    }

    final String createdId = (String) ClientServerFactory.getInstance().getDao().create(record, credential.getOperatorId());

    // ссылка на созданную запись
    final URI location = URI.create(requestURL + "/" + createdId);
    Response response = Response.created(location).build();

    return response;
  }

}

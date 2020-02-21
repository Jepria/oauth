package org.jepria.oauth.model.client.constraint;

import org.jepria.oauth.model.client.dto.ClientCreateDto;
import org.jepria.oauth.model.client.dto.ClientUpdateDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.List;

public class ApplicationGrantTypeValidator implements ConstraintValidator<ApplicationGrantType, Object> {

  @Override
  public void initialize(ApplicationGrantType applicationGrantType) {}

  @Override
  public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
    String applicationType = null;
    List<String> grantTypes = Collections.EMPTY_LIST;
    if (o instanceof ClientCreateDto) {
      ClientCreateDto dto = (ClientCreateDto) o;
      applicationType = dto.getApplicationType();
      grantTypes = dto.getGrantTypes();
    }
    if (o instanceof ClientUpdateDto) {
      ClientUpdateDto dto = (ClientUpdateDto) o;
      applicationType = dto.getApplicationType();
      grantTypes = dto.getGrantTypes();
    }
    try {
      return org.jepria.oauth.sdk.ApplicationType.implies(applicationType, grantTypes);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}

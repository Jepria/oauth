package org.jepria.oauth.client.constraint;

import org.jepria.oauth.model.client.dto.ClientCreateDto;
import org.jepria.oauth.sdk.ApplicationType;
import org.jepria.oauth.sdk.TokenAuthMethod;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TokenAuthMethodValidator implements ConstraintValidator<TokenAuthMethodConstraint, ClientCreateDto> {

  private String message;

  @Override
  public void initialize(TokenAuthMethodConstraint constraintAnnotation) {
    message = constraintAnnotation.message();
  }

  @Override
  public boolean isValid(ClientCreateDto value, ConstraintValidatorContext context) {
    if ((ApplicationType.NATIVE.equals(value.getApplicationType())  || ApplicationType.BROWSER.equals(value.getApplicationType()))
      && !TokenAuthMethod.NONE.equals(value.getTokenAuthMethod())) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(message)
        .addPropertyNode("applicationType")
        .addConstraintViolation();
      context.buildConstraintViolationWithTemplate(message)
        .addPropertyNode("tokenAuthMethod")
        .addConstraintViolation();
      return false;
    }
    return true;
  }
}

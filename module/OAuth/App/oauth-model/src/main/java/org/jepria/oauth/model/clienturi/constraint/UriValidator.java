package org.jepria.oauth.model.clienturi.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;

public class UriValidator implements ConstraintValidator<ValidURI, String> {
  @Override
  public void initialize(ValidURI validURI) {}

  @Override
  public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
    try {
      URI uri = URI.create(s);
      if (uri.getFragment() != null) {
        return false;
      }
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }
}

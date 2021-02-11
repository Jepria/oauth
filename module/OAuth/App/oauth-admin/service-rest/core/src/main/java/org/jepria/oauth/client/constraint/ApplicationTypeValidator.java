package org.jepria.oauth.client.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ApplicationTypeValidator implements ConstraintValidator<ApplicationType, String> {
  @Override
  public void initialize(ApplicationType applicationType) {}

  @Override
  public boolean isValid(String applicationType, ConstraintValidatorContext constraintValidatorContext) {
    try {
      return org.jepria.oauth.sdk.ApplicationType.implies(applicationType);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}

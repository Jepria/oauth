package org.jepria.oauth.model.client.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class GrantTypeValidator implements ConstraintValidator<GrantType, List<String>> {
  @Override
  public void initialize(GrantType grantType) {}

  @Override
  public boolean isValid(List<String> grantTypes, ConstraintValidatorContext constraintValidatorContext) {
    try {
      return grantTypes.stream().allMatch(grantType -> org.jepria.oauth.sdk.GrantType.implies(grantType));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}

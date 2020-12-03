package org.jepria.oauth.client.constraint;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = GrantTypeValidator.class)
public @interface GrantType {

  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

  String message() default "Unsupported grant type";
}

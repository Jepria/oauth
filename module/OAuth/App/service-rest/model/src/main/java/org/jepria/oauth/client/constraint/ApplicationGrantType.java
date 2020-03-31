package org.jepria.oauth.client.constraint;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ApplicationGrantTypeValidator.class)
public @interface ApplicationGrantType {

  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

  String message() default "Unsupported grant types for application type";
}

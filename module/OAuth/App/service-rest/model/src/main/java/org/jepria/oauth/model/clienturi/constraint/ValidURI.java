package org.jepria.oauth.model.clienturi.constraint;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = UriValidator.class)
public @interface ValidURI {

  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

  String message() default "Invalid URI";
}

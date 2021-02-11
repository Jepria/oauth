package org.jepria.oauth.main.rest.jersey.inject;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import java.util.Locale;
import java.util.function.Function;

public class ClientLocaleValueParamProvider implements ValueParamProvider {
  @Override
  public Function<ContainerRequest, ?> getValueProvider(Parameter parameter) {
    if (parameter.getRawType().equals(Locale.class)) {
      return new ClientLocaleSupplier();
    }
    return null;
  }

  @Override
  public PriorityType getPriority() {
    return Priority.HIGH;
  }
}

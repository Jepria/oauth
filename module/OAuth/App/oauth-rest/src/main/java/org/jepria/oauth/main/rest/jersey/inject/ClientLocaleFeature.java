package org.jepria.oauth.main.rest.jersey.inject;

import org.glassfish.jersey.InjectionManagerProvider;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.internal.process.RequestProcessingContextReference;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import javax.inject.Provider;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class ClientLocaleFeature implements Feature {
  @Override
  public boolean configure(FeatureContext featureContext) {
    InjectionManager im = InjectionManagerProvider.getInjectionManager(featureContext);

    ClientLocaleValueParamProvider authProvider = new ClientLocaleValueParamProvider();

    im.register(Bindings.service(authProvider).to(ValueParamProvider.class));

    Provider<ContainerRequest> request = () -> {
      RequestProcessingContextReference reference = im.getInstance(RequestProcessingContextReference.class);
      return reference.get().request();
    };

    im.register(Bindings.injectionResolver(new ParamInjectionResolver(authProvider, ClientLocale.class, request)));

    return true;
  }
}

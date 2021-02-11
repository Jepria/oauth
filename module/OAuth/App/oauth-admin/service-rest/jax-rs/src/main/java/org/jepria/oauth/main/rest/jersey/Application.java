package org.jepria.oauth.main.rest.jersey;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.jepria.oauth.client.ClientServerFactory;
import org.jepria.oauth.client.dao.ClientDao;
import org.jepria.oauth.client.dao.ClientDaoImpl;
import org.jepria.oauth.client.rest.ClientJaxrsAdapter;
import org.jepria.oauth.clienturi.ClientUriServerFactory;
import org.jepria.oauth.clienturi.dao.ClientUriDao;
import org.jepria.oauth.clienturi.dao.ClientUriDaoImpl;
import org.jepria.oauth.clienturi.rest.ClientUriJaxrsAdapter;
import org.jepria.oauth.key.KeyServerFactory;
import org.jepria.oauth.key.dao.KeyDao;
import org.jepria.oauth.key.dao.KeyDaoImpl;
import org.jepria.oauth.key.rest.KeyJaxrsAdapter;
import org.jepria.oauth.session.SessionServerFactory;
import org.jepria.oauth.session.dao.SessionDao;
import org.jepria.oauth.session.dao.SessionDaoImpl;
import org.jepria.oauth.session.rest.SessionJaxrsAdapter;
import org.jepria.server.service.rest.jersey.ApplicationConfigBase;

public class Application extends ApplicationConfigBase {

  public Application() {
    super();;
    register(ClientDaoImpl.class);
    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(ClientDaoImpl.class).to(ClientDao.class);
      }
    });
    register(ClientServerFactory.class);
    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(ClientServerFactory.class).to(ClientServerFactory.class).in(RequestScoped.class);
      }
    });
    register(ClientJaxrsAdapter.class);
    register(ClientUriDaoImpl.class);
    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(ClientUriDaoImpl.class).to(ClientUriDao.class);
      }
    });
    register(ClientUriServerFactory.class);
    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(ClientUriServerFactory.class).to(ClientUriServerFactory.class).in(RequestScoped.class);
      }
    });
    register(ClientUriJaxrsAdapter.class);
    register(SessionDaoImpl.class);
    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(SessionDaoImpl.class).to(SessionDao.class);
      }
    });
    register(SessionServerFactory.class);
    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(SessionServerFactory.class).to(SessionServerFactory.class).in(RequestScoped.class);
      }
    });
    register(SessionJaxrsAdapter.class);
    register(KeyDaoImpl.class);
    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(KeyDaoImpl.class).to(KeyDao.class);
      }
    });
    register(KeyServerFactory.class);
    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(KeyServerFactory.class).to(KeyServerFactory.class).in(RequestScoped.class);
      }
    });
    register(KeyJaxrsAdapter.class);
  }

  @Override
  protected void registerCorsHandler() {
  }
}
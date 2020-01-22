package org.jepria.oauth.main.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides <b>Http Basic Authentication</b> filter for JAX-RS Adapters.<br/>
 * <b>@HttpBasic</b> annotation MUST be configured with passwordType = PASSWORD/PASSWORD_HASH value for properly usage.
 * */
@javax.ws.rs.NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface WithClientCredentials {}

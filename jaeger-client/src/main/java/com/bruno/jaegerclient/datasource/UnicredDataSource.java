package com.bruno.jaegerclient.datasource;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.security.core.context.SecurityContextHolder;

public class UnicredDataSource extends AbstractRoutingDataSource {

  private final AtomicBoolean inicializado = new AtomicBoolean();

  @NotNull
  @Override
  protected DataSource determineTargetDataSource() {
    if(this.inicializado.compareAndSet(false, true)) {
      this.afterPropertiesSet();
    }
    return super.determineTargetDataSource();
  }

  @Override
  protected Object determineCurrentLookupKey() {
    var autenticacao = SecurityContextHolder.getContext().getAuthentication();
    if(autenticacao != null && autenticacao.getPrincipal() instanceof MultiDSUnicred user){
      var tenantId = user.getTenantId();
      System.out.println("the tenantId is: " + tenantId);
      return tenantId;
    }
    return null;
  }

}

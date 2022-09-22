package com.bruno.jaegerclient.routing;

import com.bruno.jaegerclient.datasource.MultiDSUnicred;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.security.core.context.SecurityContextHolder;

public class UnicredDataSourceRouting extends AbstractRoutingDataSource {

  private final AtomicBoolean started = new AtomicBoolean();

  @NotNull
  @Override
  protected DataSource determineTargetDataSource() {
    if(this.started.compareAndSet(false, true)) {
      this.afterPropertiesSet();
    }
    return super.determineTargetDataSource();
  }

  @Override
  protected Object determineCurrentLookupKey() {
    var autenticacao = SecurityContextHolder.getContext().getAuthentication();
    if(autenticacao != null && autenticacao.getPrincipal() instanceof MultiDSUnicred user){
      var tenantId = user.getTenantId();
      logger.info("the tenantId is: " + tenantId);
      return tenantId;
    }
    return null;
  }

}

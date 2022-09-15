package com.bruno.jaegerclient;

import static org.springframework.web.servlet.function.RouterFunctions.route;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@SpringBootApplication
public class JaegerClientApplication {

    public static void main(String[] args) { SpringApplication.run(JaegerClientApplication.class, args); }
        @Bean
        RouterFunction<ServerResponse> routes (JdbcTemplate template){
            return route()
                .GET("/customers",  request -> {
                    var results = template.query("select * from customer",
                        (rs, rowNum) -> new Customer(rs.getInt("id"), rs.getString("name")));
                    return ServerResponse.ok().body(results);
                })
                .build();
        }
}

@Configuration
class DataSourceConfiguration {

  @Bean
  @Primary
  DataSource unicredDataSource(Map<String, DataSource> dataSources) {
    var prefix = "ds";
    var map = dataSources
        .entrySet()
        .stream()
        .filter( e -> e.getKey().startsWith(prefix))
        .collect(Collectors.toMap(
            e -> (Object) Integer.parseInt(e.getKey().substring(prefix.length())),
            e -> (Object) e.getValue()
        ));

    map.forEach((tenentId, ds) -> {
      var inicializada = new ResourceDatabasePopulator(new ClassPathResource("schema.sql") ,
          new ClassPathResource(prefix + tenentId + "-data.sql"));
      inicializada.execute((DataSource) ds);
      System.out.println("Inicializando dataSource: " + tenentId);
    });

    var uds = new UnicredDataSource();
    uds.setTargetDataSources(map);
    return uds;

  }
}

class UnicredDataSource extends AbstractRoutingDataSource {

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

    return null;
  }
}

record Customer (Integer id, String name){

}
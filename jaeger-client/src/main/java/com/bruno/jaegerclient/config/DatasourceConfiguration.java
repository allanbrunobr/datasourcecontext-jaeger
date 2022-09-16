package com.bruno.jaegerclient.config;

import com.bruno.jaegerclient.datasource.UnicredDataSource;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
public class DatasourceConfiguration {

    @Bean
    @Primary
    DataSource unicredDataSource(Map <String, DataSource> dataSources) {
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

    @Bean
    DataSource ds1(){
    return dataSource(5431);
  }

    @Bean
    DataSource ds2(){
    return dataSource(5432);
  }

    private static DataSource dataSource(int port) {
    var dsp = new DataSourceProperties();
    dsp.setPassword("pw");
    dsp.setUsername("user");
    dsp.setUrl("jdbc:postgresql://localhost:" + port + "/postgres");
    return dsp.initializeDataSourceBuilder()
        .type(HikariDataSource.class)
        .build();
  }



}

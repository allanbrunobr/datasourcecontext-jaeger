package com.bruno.jaegerclient.config;

import com.bruno.jaegerclient.routing.UnicredDataSourceRouting;
import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
public class DatasourceConfiguration {


  private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceConfiguration.class);

  private static final String SCHEMA_POSTGRES = "schema-postgres.sql";

  private static final String PREFIX_POSTGRES = "ds";

  private static final String NOME_ARQUIVO_POSTGRES = "-datapostgres.sql";

  private static final String SCHEMA_SQLSERVER = "schema-sqlserver.sql";

  private static final String PREFIX_SQLSERVER = "sqlServer";

  private static final String NOME_ARQUIVOSQLSERVER = "-data-sqlserver.sql";


    @Bean
    @Primary
    DataSource loadDataSources(Map <String, DataSource> dataSources) {

      var mapPostgres = getDSByPrefix(PREFIX_POSTGRES, SCHEMA_POSTGRES, NOME_ARQUIVO_POSTGRES, dataSources);
      var mapSQlServer = getDSByPrefix(PREFIX_SQLSERVER, SCHEMA_SQLSERVER, NOME_ARQUIVOSQLSERVER, dataSources);

      List<Map<Object, Object>> totalDS = new ArrayList<>();
      totalDS.add(mapPostgres);

      var uds = new UnicredDataSourceRouting();
      uds.setTargetDataSources(totalDS.stream()
                              .reduce((mapPS, mapSQL) -> {
                                mapPostgres.putAll(mapSQlServer);
                                return mapPS;
                              }).orElse(null));
      return uds;

  }

  private  Map<Object, Object> getDSByPrefix(String prefix, String schema, String extNomeArquivo, Map <String, DataSource> dataSource){
    var map = dataSource
        .entrySet()
        .stream()
        .filter( e -> e.getKey().startsWith(prefix))
        .collect(Collectors.toMap(
            e -> (Object) Integer.parseInt(e.getKey().substring(prefix.length())),
            e -> (Object) e.getValue()
        ));

    map.forEach((tenantId, ds) -> {
      var starter = new ResourceDatabasePopulator(new ClassPathResource(schema) ,
          new ClassPathResource(prefix + tenantId + extNomeArquivo));
      starter.execute((DataSource) ds);
      LOGGER.info("Inicializando dataSource: {} ", tenantId);
    });
    return map;

  }

    @Bean
    DataSource ds1(){
    return dataSourcePostgres(5431, "user", "pw");
  }

    @Bean
    DataSource ds2(){
    return dataSourcePostgres(5432, "user", "pw");
  }


    @Bean
    DataSource sqlServer1(){
      return dataSourceSQLServer(15785);
    }

    @Bean
    DataSource sqlServer2(){
      return dataSourceSQLServer(15786);
    }

    private static DataSource dataSourcePostgres(int port, String user, String pass) {
    var dsp = new DataSourceProperties();
    dsp.setPassword(pass);
    dsp.setUsername(user);
    dsp.setUrl("jdbc:postgresql://localhost:" + port + "/postgres");
    return dsp.initializeDataSourceBuilder()
        .type(HikariDataSource.class)
        .build();
  }

  private static DataSource dataSourceSQLServer(int port) {
    var dsp = new DataSourceProperties();
    dsp.setPassword("@Un1cr3D");
    dsp.setUsername("sa");
    dsp.setUrl("jdbc:sqlserver://localhost:" + port +
                      "; DatabaseName=master; encrypt=true;trustServerCertificate=true");
    return dsp.initializeDataSourceBuilder()
        .type(HikariDataSource.class)
        .build();
  }


}

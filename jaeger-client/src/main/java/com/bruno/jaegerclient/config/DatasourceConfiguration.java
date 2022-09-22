package com.bruno.jaegerclient.config;

import com.bruno.jaegerclient.routing.UnicredDataSourceRouting;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import one.util.streamex.EntryStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
  private static final String PREFIX_SQLSERVER = "sqlserver";
  private static final String NOME_ARQUIVO_SQLSERVER = "-data-sqlserver.sql";

  @Value("${service.postgres.db1.port}")
  private int POSTGRES_DB1_PORT;
  @Value("${service.postgres.db2.port}")
  private int POSTGRES_DB2_PORT;
  @Value("${service.sqlserver.sqlserver1.port}")
  private int SQLSERVER_SS1_PORT;
  @Value("${service.sqlserver.sqlserver2.port}")
  private int SQLSERVER_SS2_PORT;
  @Value("${service.postgres.db.user}")
  private String POSTGRES_DB_USER;
  @Value("${service.postgres.db.pass}")
  private String POSTGRES_DB_PASS;
  @Value("${service.sqlserver.db.user}")
  private String SQLSERVER_DB_USER;
  @Value("${service.sqlserver.db.pass}")
  private String SQLSERVER_DB_PASS;

    @Bean
    @Primary
    DataSource loadDataSources(Map <String, DataSource> dataSources) {

      var mapPostgres = getDataSourceByPrefixSchemaExt(
                                              PREFIX_POSTGRES, SCHEMA_POSTGRES, NOME_ARQUIVO_POSTGRES, dataSources);
      var mapSQlServer = getDataSourceByPrefixSchemaExt(
                                              PREFIX_SQLSERVER, SCHEMA_SQLSERVER, NOME_ARQUIVO_SQLSERVER, dataSources);

      Map<Object, Object> mapTotal = EntryStream.of(mapPostgres)
          .append(EntryStream.of(mapSQlServer))
          .toMap((e1, e2) -> e1);

      var uds = new UnicredDataSourceRouting();
      uds.setTargetDataSources(mapTotal);
      return uds;
  }
  private  Map<Object, Object> getDataSourceByPrefixSchemaExt(String prefix, String schema, String extNomeArquivo,
                                                      Map <String, DataSource> dataSource){
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
    return dataSourcePostgres(POSTGRES_DB1_PORT, POSTGRES_DB_USER, POSTGRES_DB_PASS);
  }
    @Bean
    DataSource ds2(){
    return dataSourcePostgres(POSTGRES_DB2_PORT, POSTGRES_DB_USER, POSTGRES_DB_PASS);
  }
    @Bean
    DataSource sqlserver3(){
      return dataSourceSQLServer(SQLSERVER_SS1_PORT,SQLSERVER_DB_USER, SQLSERVER_DB_PASS);
    }
    @Bean
    DataSource sqlserver4(){
      return dataSourceSQLServer(SQLSERVER_SS2_PORT,SQLSERVER_DB_USER, SQLSERVER_DB_PASS);
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

  private static DataSource dataSourceSQLServer(int port, String user, String pass) {
    var dsp = new DataSourceProperties();
    dsp.setPassword(pass);
    dsp.setUsername(user);
    dsp.setUrl("jdbc:sqlserver://localhost:" + port +
                      "; DatabaseName=master; encrypt=true;trustServerCertificate=true");
    return dsp.initializeDataSourceBuilder()
        .type(HikariDataSource.class)
        .build();
  }

}
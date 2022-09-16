package com.bruno.jaegerclient;

import static org.springframework.web.servlet.function.RouterFunctions.route;

import com.zaxxer.hikari.HikariDataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.SecurityFilterChain;
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

  @Bean
  DataSource ds1(){
    return dataSource(5431);
  }

  @Bean
  DataSource ds2(){
    return dataSource(5432);
  }

  private static DataSource dataSource( int port) {
    var dsp = new DataSourceProperties();
    dsp.setPassword("pw");
    dsp.setUsername("user");
    dsp.setUrl("jdbc:postgresql://localhost:" + port + "/postgres");
    return dsp.initializeDataSourceBuilder()
        .type(HikariDataSource.class)
        .build();
  }

}

@Configuration
class SecurityConfiguration {
  @Bean
  SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
    httpSecurity.httpBasic(Customizer.withDefaults())
        .authorizeHttpRequests( auth -> auth.anyRequest().authenticated())
        .csrf(AbstractHttpConfigurer::disable);

      return httpSecurity.build();

  }

  @Bean
  UserDetailsService userDetailsService () {
    var ab = createUser("abruno", 1);
    var os = createUser("osilva", 2);
    var users = Stream.of(os, ab)
        .collect(Collectors.toMap(User::getUsername, u -> u));
    return username -> {
        var user = users.getOrDefault( username, null);
        if ( user == null)
          throw new UsernameNotFoundException("Usuário não encontrado" + username);
        return user;
    };
  }

  private static User createUser(String name, Integer tenantId){
    return new MultiDSUnicred(name,
        "pw", true, true, true, true, tenantId);
  }
  
}

class MultiDSUnicred  extends User {

  private final Integer tenantId;
  public Integer getTenantId() {
    return tenantId;
  }

  public MultiDSUnicred(String username, String password, boolean enabled,
      boolean accountNonExpired,
      boolean credentialsNonExpired, boolean accountNonLocked ,
      Integer tenantId) {

    super(username, PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password),
        enabled, accountNonExpired, credentialsNonExpired, accountNonLocked,
        List.of(new SimpleGrantedAuthority("USER")));
    this.tenantId = tenantId;
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
    if(autenticacao != null && autenticacao.getPrincipal() instanceof MultiDSUnicred user){
      var tenantId = user.getTenantId();
      System.out.println("the tenantId is: " + tenantId);
      return tenantId;
    }
    return null;
  }
}

record Customer (Integer id, String name){

}
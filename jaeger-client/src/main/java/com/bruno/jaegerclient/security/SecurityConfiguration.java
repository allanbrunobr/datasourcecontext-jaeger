package com.bruno.jaegerclient.security;


import com.bruno.jaegerclient.datasource.MultiDSUnicred;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfiguration {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.httpBasic(Customizer.withDefaults())
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .csrf(AbstractHttpConfigurer::disable);

    return httpSecurity.build();

  }

  @Bean
  UserDetailsService userDetailsService () {
    var ab = createUser("abruno", "pw",1);
    var os = createUser("osilva", "pw",2);
    var coop4022 = createUser("coop4022","@Un1cr3D", 3);
    var coop505 = createUser("coop505", "@Un1cr3D", 4);
    var users = Stream.of(os, ab, coop4022, coop505)
        .collect(Collectors.toMap(User::getUsername, u -> u));
    return username -> {
      var user = users.getOrDefault( username, null);
      if ( user == null)
        throw new UsernameNotFoundException("Usuário não encontrado" + username);
      return user;
    };
  }

  private static User createUser(String name, String pass, Integer tenantId){
    return new MultiDSUnicred(name,
        pass, true, true, true, true, List.of(new SimpleGrantedAuthority("USER")), tenantId);
  }

}

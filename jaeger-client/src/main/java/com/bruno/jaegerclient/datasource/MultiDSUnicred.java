package com.bruno.jaegerclient.datasource;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

public class MultiDSUnicred extends User {

  private final Integer tenantId;

  public Integer getTenantId() {
    return tenantId;
  }

  public MultiDSUnicred(String username, String password, boolean enabled,
      boolean accountNonExpired,
      boolean credentialsNonExpired, boolean accountNonLocked , Collection<? extends GrantedAuthority> authorities,
      Integer tenantId) {

    super(username, PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password),
        enabled, accountNonExpired, credentialsNonExpired, accountNonLocked,
        List.of(new SimpleGrantedAuthority("USER")) );
    this.tenantId = tenantId;
  }


}

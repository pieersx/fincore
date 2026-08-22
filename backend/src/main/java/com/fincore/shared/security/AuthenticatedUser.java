package com.fincore.shared.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fincore.identity.entity.Role;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Principal autenticado que transporta solamente la identidad necesaria para autorizar.
 */
public final class AuthenticatedUser implements UserDetails, CredentialsContainer {

    private final UUID id;
    private final String username;
    private String password;
    private final Set<Role> roles;
    private final boolean enabled;

    public AuthenticatedUser(UUID id, String username, String password, Set<Role> roles, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = Set.copyOf(roles);
        this.enabled = enabled;
    }

    public UUID id() {
        return id;
    }

    public Set<Role> roles() {
        return Collections.unmodifiableSet(roles);
    }

    /** Spring Security espera el prefijo ROLE_ cuando se utiliza hasRole. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /** Elimina el hash de la contraseña del principal después de autenticar. */
    @Override
    public void eraseCredentials() {
        password = null;
    }
}

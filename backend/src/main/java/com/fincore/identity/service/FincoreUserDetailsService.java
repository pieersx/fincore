package com.fincore.identity.service;

import java.util.Locale;

import com.fincore.identity.entity.UserAccount;
import com.fincore.identity.repository.UserAccountRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class FincoreUserDetailsService implements UserDetailsService {

    private final UserAccountRepository repository;

    FincoreUserDetailsService(UserAccountRepository repository) {
        this.repository = repository;
    }

    /** Normaliza el identificador para evitar cuentas duplicadas por mayúsculas. */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        return repository.findByUsername(normalizedUsername)
                .map(UserAccount::toPrincipal)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
    }
}

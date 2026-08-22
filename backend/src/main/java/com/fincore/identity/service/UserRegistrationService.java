package com.fincore.identity.service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import com.fincore.identity.entity.Role;
import com.fincore.identity.entity.UserStatus;
import com.fincore.identity.dto.UserView;
import com.fincore.identity.entity.UserAccount;
import com.fincore.identity.mapper.UserMapper;
import com.fincore.identity.repository.UserAccountRepository;
import com.fincore.shared.exception.ConflictException;
import com.fincore.shared.exception.OperationNotAllowedException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRegistrationService {

    private static final Pattern VALID_USERNAME = Pattern.compile("[a-z0-9._-]{4,50}");

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private final Clock clock;

    UserRegistrationService(
            UserAccountRepository repository,
            PasswordEncoder passwordEncoder,
            UserMapper mapper,
            Clock clock) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.clock = clock;
    }

    /** Este servicio crea la identidad; onboarding coordina después el perfil del cliente. */
    @Transactional
    public UserView registerCustomerIdentity(String username, String password) {
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        if (!VALID_USERNAME.matcher(normalizedUsername).matches()) {
            throw new OperationNotAllowedException("El nombre de usuario no cumple el formato permitido.");
        }
        if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new OperationNotAllowedException("La contraseña no cumple el tamaño seguro para BCrypt.");
        }
        if (repository.existsByUsername(normalizedUsername)) {
            throw new ConflictException("El nombre de usuario ya está registrado.");
        }

        Instant now = Instant.now(clock);
        UserAccount account = new UserAccount(
                UUID.randomUUID(),
                normalizedUsername,
                passwordEncoder.encode(password),
                UserStatus.ACTIVE,
                Set.of(Role.CUSTOMER),
                now);

        try {
            return mapper.toView(repository.saveAndFlush(account));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("El nombre de usuario ya está registrado.");
        }
    }
}

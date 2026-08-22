package com.fincore.identity.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.fincore.audit.entity.AuditOutcome;
import com.fincore.audit.service.AuditService;
import com.fincore.shared.security.AuthenticatedUser;
import com.fincore.identity.entity.Role;
import com.fincore.identity.entity.UserStatus;
import com.fincore.identity.dto.UserView;
import com.fincore.identity.entity.UserAccount;
import com.fincore.identity.mapper.UserMapper;
import com.fincore.identity.repository.UserAccountRepository;
import com.fincore.shared.exception.ConflictException;
import com.fincore.shared.exception.OperationNotAllowedException;
import com.fincore.shared.exception.ResourceNotFoundException;
import com.fincore.shared.dto.PageResponse;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdministrationService {

    private final UserAccountRepository repository;
    private final AuditService auditService;
    private final UserMapper mapper;
    private final Clock clock;

    UserAdministrationService(
            UserAccountRepository repository,
            AuditService auditService,
            UserMapper mapper,
            Clock clock) {
        this.repository = repository;
        this.auditService = auditService;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserView> findAll(int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by("username").ascending());
        return PageResponse.from(repository.findAll(request), mapper::toView);
    }

    /** Impide que el administrador se bloquee a sí mismo o elimine al último administrador activo. */
    @Transactional
    public UserView changeStatus(UUID userId, UserStatus newStatus, AuthenticatedUser administrator) {
        UserAccount account = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no existe."));

        if (account.id().equals(administrator.id()) && newStatus == UserStatus.SUSPENDED) {
            throw new OperationNotAllowedException("Un administrador no puede suspender su propia cuenta.");
        }
        if (account.roles().contains(Role.ADMIN)
                && newStatus == UserStatus.SUSPENDED
                && repository.countByStatusAndRole(UserStatus.ACTIVE, Role.ADMIN) <= 1) {
            throw new ConflictException("Debe permanecer al menos un administrador activo.");
        }

        account.changeStatus(newStatus, Instant.now(clock));
        auditService.record(
                administrator.getUsername(),
                "USER_STATUS_CHANGED",
                AuditOutcome.SUCCESS,
                "USER",
                userId.toString(),
                "Nuevo estado: " + newStatus);
        return mapper.toView(account);
    }
}

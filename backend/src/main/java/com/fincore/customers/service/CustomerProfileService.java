package com.fincore.customers.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.fincore.audit.entity.AuditOutcome;
import com.fincore.audit.service.AuditService;
import com.fincore.customers.dto.CustomerProfileView;
import com.fincore.customers.entity.CustomerStatus;
import com.fincore.customers.entity.CustomerProfile;
import com.fincore.customers.mapper.CustomerMapper;
import com.fincore.customers.repository.CustomerProfileRepository;
import com.fincore.shared.security.AuthenticatedUser;
import com.fincore.shared.exception.OperationNotAllowedException;
import com.fincore.shared.exception.ResourceNotFoundException;
import com.fincore.shared.dto.PageResponse;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerProfileService {

    private final CustomerProfileRepository repository;
    private final AuditService auditService;
    private final CustomerMapper mapper;
    private final Clock clock;

    CustomerProfileService(
            CustomerProfileRepository repository,
            AuditService auditService,
            CustomerMapper mapper,
            Clock clock) {
        this.repository = repository;
        this.auditService = auditService;
        this.mapper = mapper;
        this.clock = clock;
    }

    /** La funcionalidad de identidad entrega solo su UUID; esta capa mantiene el perfil. */
    @Transactional
    public CustomerProfileView create(UUID userId, String displayName) {
        String normalizedDisplayName = displayName.trim();
        if (normalizedDisplayName.length() < 2) {
            throw new OperationNotAllowedException("El nombre visible debe contener al menos 2 caracteres.");
        }
        CustomerProfile profile = new CustomerProfile(
                UUID.randomUUID(),
                userId,
                normalizedDisplayName,
                Instant.now(clock));
        return mapper.toView(repository.save(profile));
    }

    /** Usa el UUID del principal para impedir que un cliente elija el perfil de otra persona. */
    @Transactional(readOnly = true)
    public CustomerProfileView findOwnProfile(AuthenticatedUser user) {
        return repository.findByUserId(user.id())
                .map(mapper::toView)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un perfil para el usuario autenticado."));
    }

    /**
     * Centraliza la regla que impide operar financieramente a un cliente suspendido.
     */
    @Transactional(readOnly = true)
    public CustomerProfileView requireActiveByUserId(UUID userId) {
        CustomerProfileView customer = repository.findByUserId(userId)
                .map(mapper::toView)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un cliente para el usuario autenticado."));
        if (customer.status() != CustomerStatus.ACTIVE) {
            throw new OperationNotAllowedException("El cliente está suspendido y no puede realizar operaciones.");
        }
        return customer;
    }

    @Transactional(readOnly = true)
    public CustomerProfileView findById(UUID customerId) {
        return repository.findById(customerId)
                .map(mapper::toView)
                .orElseThrow(() -> new ResourceNotFoundException("El cliente no existe."));
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerProfileView> findAll(int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by("displayName").ascending());
        return PageResponse.from(repository.findAll(request), mapper::toView);
    }

    @Transactional
    public CustomerProfileView changeStatus(
            UUID customerId,
            CustomerStatus newStatus,
            AuthenticatedUser administrator) {
        CustomerProfile profile = repository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("El cliente no existe."));
        profile.changeStatus(newStatus, Instant.now(clock));
        auditService.record(
                administrator.getUsername(),
                "CUSTOMER_STATUS_CHANGED",
                AuditOutcome.SUCCESS,
                "CUSTOMER",
                customerId.toString(),
                "Nuevo estado: " + newStatus);
        return mapper.toView(profile);
    }
}

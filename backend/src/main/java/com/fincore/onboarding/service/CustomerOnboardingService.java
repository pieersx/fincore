package com.fincore.onboarding.service;

import java.util.List;

import com.fincore.accounts.service.FinancialAccountService;
import com.fincore.accounts.dto.AccountView;
import com.fincore.audit.entity.AuditOutcome;
import com.fincore.audit.service.AuditService;
import com.fincore.customers.dto.CustomerProfileView;
import com.fincore.customers.service.CustomerProfileService;
import com.fincore.identity.service.UserRegistrationService;
import com.fincore.identity.dto.UserView;
import com.fincore.shared.exception.OperationNotAllowedException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerOnboardingService {

    private final UserRegistrationService userService;
    private final CustomerProfileService customerService;
    private final FinancialAccountService accountService;
    private final AuditService auditService;
    private final boolean registrationEnabled;

    CustomerOnboardingService(
            UserRegistrationService userService,
            CustomerProfileService customerService,
            FinancialAccountService accountService,
            AuditService auditService,
            @Value("${fincore.identity.registration-enabled:false}") boolean registrationEnabled) {
        this.userService = userService;
        this.customerService = customerService;
        this.accountService = accountService;
        this.auditService = auditService;
        this.registrationEnabled = registrationEnabled;
    }

    /** Si falla cualquiera de las funcionalidades coordinadas, la transacción revierte el alta completa. */
    @Transactional
    public RegisteredCustomer register(String username, String password, String displayName) {
        if (!registrationEnabled) {
            throw new OperationNotAllowedException("El registro público está deshabilitado.");
        }
        UserView user = userService.registerCustomerIdentity(username, password);
        CustomerProfileView customer = customerService.create(user.id(), displayName);
        List<AccountView> accounts = accountService.provisionDefaultAccounts(customer.id());
        auditService.record(
                user.username(),
                "USER_REGISTERED",
                AuditOutcome.SUCCESS,
                "USER",
                user.id().toString(),
                "Registro de cliente sintético");
        return new RegisteredCustomer(user, customer, accounts);
    }

    public record RegisteredCustomer(UserView user, CustomerProfileView customer, List<AccountView> accounts) {
    }
}

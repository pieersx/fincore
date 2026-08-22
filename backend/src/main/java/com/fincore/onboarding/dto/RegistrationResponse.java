package com.fincore.onboarding.dto;

import java.util.List;

import com.fincore.accounts.dto.AccountView;
import com.fincore.customers.dto.CustomerProfileView;
import com.fincore.identity.dto.UserView;

/** Resultado completo del alta coordinada del cliente. */
public record RegistrationResponse(
        UserView user,
        CustomerProfileView customer,
        List<AccountView> accounts) {
}

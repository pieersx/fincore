package com.fincore.accounts.mapper;

import com.fincore.accounts.dto.AccountInfo;
import com.fincore.accounts.dto.AccountView;
import com.fincore.accounts.entity.FinancialAccount;

import org.springframework.stereotype.Component;

/** Convierte la entidad de cuenta en DTO sin exponer detalles de JPA. */
@Component
public class AccountMapper {

    public AccountView toView(FinancialAccount account) {
        return new AccountView(
                account.id(),
                account.customerId(),
                account.accountNumber(),
                account.kind(),
                account.currency(),
                account.status(),
                account.balance(),
                account.createdAt(),
                account.updatedAt());
    }

    public AccountInfo toInfo(FinancialAccount account) {
        return new AccountInfo(
                account.id(),
                account.customerId(),
                account.accountNumber(),
                account.kind(),
                account.currency(),
                account.status());
    }
}

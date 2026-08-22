package com.fincore.beneficiaries.mapper;

import com.fincore.accounts.dto.AccountInfo;
import com.fincore.beneficiaries.dto.BeneficiaryInfo;
import com.fincore.beneficiaries.dto.BeneficiaryView;
import com.fincore.beneficiaries.entity.Beneficiary;

import org.springframework.stereotype.Component;

/** Combina el beneficiario persistido con los datos visibles de su cuenta destino. */
@Component
public class BeneficiaryMapper {

    public BeneficiaryInfo toInfo(Beneficiary beneficiary) {
        return new BeneficiaryInfo(beneficiary.id(), beneficiary.destinationAccountId());
    }

    public BeneficiaryView toView(Beneficiary beneficiary, AccountInfo account) {
        return new BeneficiaryView(
                beneficiary.id(),
                beneficiary.destinationAccountId(),
                account.accountNumber(),
                account.currency(),
                beneficiary.alias(),
                beneficiary.createdAt());
    }
}

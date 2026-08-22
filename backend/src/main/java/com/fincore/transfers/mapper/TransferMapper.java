package com.fincore.transfers.mapper;

import com.fincore.accounts.dto.AccountInfo;
import com.fincore.transfers.dto.TransferView;
import com.fincore.transfers.entity.FinancialTransfer;

import org.springframework.stereotype.Component;

/** Construye la respuesta de una transferencia con los números de cuenta relacionados. */
@Component
public class TransferMapper {

    public TransferView toView(
            FinancialTransfer transfer,
            AccountInfo source,
            AccountInfo destination) {
        return new TransferView(
                transfer.id(),
                transfer.reference(),
                transfer.createdByUserId(),
                transfer.sourceAccountId(),
                source.accountNumber(),
                transfer.destinationAccountId(),
                destination.accountNumber(),
                transfer.beneficiaryId(),
                transfer.currency(),
                transfer.amount(),
                transfer.status(),
                transfer.description(),
                transfer.createdAt(),
                transfer.completedAt());
    }
}

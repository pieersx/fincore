package com.fincore.transfers.controller;

import java.net.URI;
import java.util.UUID;

import com.fincore.shared.security.AuthenticatedUser;
import com.fincore.shared.dto.PageResponse;
import com.fincore.transfers.dto.CreateTransferRequest;
import com.fincore.transfers.dto.TransferView;
import com.fincore.transfers.dto.TransferCommand;
import com.fincore.transfers.service.TransferReceiptService;
import com.fincore.transfers.service.TransferService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
@Validated
class TransferController {

    private final TransferService service;
    private final TransferReceiptService receiptService;

    TransferController(TransferService service, TransferReceiptService receiptService) {
        this.service = service;
        this.receiptService = receiptService;
    }

    @PostMapping
    ResponseEntity<TransferView> create(
            @RequestHeader("Idempotency-Key")
            @Pattern(
                    regexp = "[A-Za-z0-9._:-]{8,100}",
                    message = "debe tener entre 8 y 100 caracteres válidos")
            String idempotencyKey,
            @Valid @RequestBody CreateTransferRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        TransferView transfer = service.create(
                idempotencyKey,
                new TransferCommand(
                        request.sourceAccountId(),
                        request.beneficiaryId(),
                        request.amount(),
                        request.description()),
                user);
        return ResponseEntity
                .created(URI.create("/api/v1/transfers/" + transfer.id()))
                .body(transfer);
    }

    @GetMapping
    PageResponse<TransferView> findOwn(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return service.findOwn(user, page, size);
    }

    @GetMapping("/{transferId}")
    TransferView findOwnById(
            @PathVariable UUID transferId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return service.findOwnById(transferId, user);
    }

    @GetMapping(value = "/{transferId}/receipt", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<byte[]> receipt(
            @PathVariable UUID transferId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        TransferView transfer = service.findOwnById(transferId, user);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("fincore-" + transfer.reference() + ".pdf")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(receiptService.generate(transfer));
    }
}

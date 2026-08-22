package com.fincore.accounts.dto;

import java.time.Instant;
import java.util.List;

public record ReconciliationView(
        Instant generatedAt,
        boolean balanced,
        long mismatchCount,
        List<ReconciliationItem> accounts) {
}

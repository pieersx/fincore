package com.fincore.transfers.repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Usa INSERT ... ON CONFLICT para que PostgreSQL decida cuál solicitud concurrente posee la clave.
 */
@Repository
public class TransferIdempotencyRepository {

    private final JdbcClient jdbcClient;

    TransferIdempotencyRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public IdempotencyClaim claim(UUID actorUserId, String key, String requestHash, Instant createdAt) {
        UUID reservationId = UUID.randomUUID();
        int inserted = jdbcClient.sql("""
                        insert into transfer_idempotency
                            (id, actor_user_id, idempotency_key, request_hash, created_at)
                        values (:id, :actorUserId, :key, :requestHash, :createdAt)
                        on conflict (actor_user_id, idempotency_key) do nothing
                        """)
                .param("id", reservationId)
                .param("actorUserId", actorUserId)
                .param("key", key)
                .param("requestHash", requestHash)
                .param("createdAt", Timestamp.from(createdAt))
                .update();
        if (inserted == 1) {
            return new IdempotencyClaim(reservationId, requestHash, null, true);
        }
        return jdbcClient.sql("""
                        select id, request_hash, transfer_id
                        from transfer_idempotency
                        where actor_user_id = :actorUserId and idempotency_key = :key
                        """)
                .param("actorUserId", actorUserId)
                .param("key", key)
                .query((resultSet, rowNumber) -> new IdempotencyClaim(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getString("request_hash"),
                        resultSet.getObject("transfer_id", UUID.class),
                        false))
                .single();
    }

    public void complete(UUID reservationId, UUID transferId, Instant completedAt) {
        jdbcClient.sql("""
                        update transfer_idempotency
                        set transfer_id = :transferId, completed_at = :completedAt
                        where id = :reservationId and transfer_id is null
                        """)
                .param("transferId", transferId)
                .param("completedAt", Timestamp.from(completedAt))
                .param("reservationId", reservationId)
                .update();
    }

    public record IdempotencyClaim(
            UUID reservationId,
            String requestHash,
            UUID transferId,
            boolean newlyCreated) {
    }
}

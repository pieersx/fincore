package com.fincore.identity.repository;

import java.util.Optional;
import java.util.UUID;

import com.fincore.identity.entity.Role;
import com.fincore.identity.entity.UserStatus;
import com.fincore.identity.entity.UserAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("""
            select count(distinct user)
            from UserAccount user
            join user.roles role
            where user.status = :status and role = :role
            """)
    long countByStatusAndRole(@Param("status") UserStatus status, @Param("role") Role role);
}

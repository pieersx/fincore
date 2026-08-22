package com.fincore.identity.mapper;

import com.fincore.identity.dto.UserView;
import com.fincore.identity.entity.UserAccount;

import org.springframework.stereotype.Component;

/** Evita que la entidad o el hash de contraseña se devuelvan desde la API. */
@Component
public class UserMapper {

    public UserView toView(UserAccount account) {
        return new UserView(
                account.id(),
                account.username(),
                account.status(),
                account.roles(),
                account.createdAt(),
                account.updatedAt());
    }
}

package com.fincore.customers.mapper;

import com.fincore.customers.dto.CustomerProfileView;
import com.fincore.customers.entity.CustomerProfile;

import org.springframework.stereotype.Component;

/** Construye DTO seguros a partir del perfil persistido. */
@Component
public class CustomerMapper {

    public CustomerProfileView toView(CustomerProfile profile) {
        return new CustomerProfileView(
                profile.id(),
                profile.userId(),
                profile.displayName(),
                profile.status(),
                profile.createdAt(),
                profile.updatedAt());
    }
}

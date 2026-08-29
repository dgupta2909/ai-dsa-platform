package com.aidsa.platform.security;

import java.security.Principal;

public record UserPrincipal(
        Long id,
        String fullName,
        String email
) implements Principal {
    @Override
    public String getName() {
        return email;
    }
}

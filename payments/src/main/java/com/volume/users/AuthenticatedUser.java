package com.volume.users;

import com.volume.shared.domain.types.UserId;
import lombok.Value;

import java.util.UUID;

@Value
public class AuthenticatedUser {
    private static AuthenticatedUser ADMIN = new AuthenticatedUser(UserId.Companion.random());
    private static AuthenticatedUser SUPPORT = new AuthenticatedUser(UserId.Companion.random());
    private static AuthenticatedUser MERCHANT = new AuthenticatedUser(UserId.Companion.random());
    private static AuthenticatedUser SHOPPER = new AuthenticatedUser(UserId.Companion.random());
    private final UserId userId;

    public static AuthenticatedUser admin() {
        return ADMIN;
    }
}

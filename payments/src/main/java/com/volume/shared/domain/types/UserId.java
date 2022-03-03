package com.volume.shared.domain.types;

import com.volume.shared.infrastructure.persistence.ValueObject;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

@Value
public class UserId implements ValueObject, Serializable {
    private final UUID value;

    public UserId(UUID value) {
        // validate here
        this.value = value;
    }

    public static UserId random() {
        return new UserId(UUID.randomUUID());
    }
}

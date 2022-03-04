package com.volume.shared.infrastructure.persistence;

public interface ValueObject {
    /**
     * I have a lot of transitions from Domain/Persistence layer to Dto.
     * I always (to be verified) require transition to string (until I'll find a way to properly serialize ValueObjects as single value in Jackson.
     * I found it kinda error prone to do that valueObject.getValue().toString()
     * I it seems like valueObject.asString is easier and cleaner. I leave it like this until I find a better solution.
     */
    String asString();
}

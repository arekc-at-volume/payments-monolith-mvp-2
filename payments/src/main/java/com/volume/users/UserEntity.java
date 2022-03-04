package com.volume.users;

import com.google.common.base.Preconditions;
import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRoot;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.time.LocalDateTime;

@Entity
@Getter // TODO: how can we not expose these getters? How can we assert in tests and map between layers without it?
@Inheritance(strategy = InheritanceType.JOINED)
class UserEntity extends BaseKeyedVersionedAggregateRoot<UserId> {
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
    private UserId updateBy;

    protected UserEntity() { super(); }

    /**
     * Constructor made protected to prevent direct creation of UserEntity
     */
    protected UserEntity(UserId id, UserId updateBy) {
        super(id);

        Preconditions.checkNotNull(updateBy);

        this.createAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.updateBy = updateBy;
    }
}


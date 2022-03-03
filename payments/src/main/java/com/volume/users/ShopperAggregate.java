package com.volume.users;

import com.volume.shared.domain.types.UserId;
import lombok.Getter;

import javax.persistence.Entity;

@Entity
@Getter
class ShopperAggregate extends UserEntity {
    protected ShopperAggregate() {
        super();
    }

    public ShopperAggregate(UserId id, UserId updateBy) {
        super(id, updateBy);
    }
}

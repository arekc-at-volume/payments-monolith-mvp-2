package com.volume.users;

import com.volume.shared.domain.types.UserId;
import lombok.Getter;

import javax.persistence.Entity;

@Entity
@Getter
class MerchantAggregate extends UserEntity {
    private String clientSecret;

    protected MerchantAggregate() {
        super();
    }

    public MerchantAggregate(UserId id, UserId updateBy, String clientSecret) {
        super(id, updateBy);
        this.clientSecret = clientSecret;
    }

    public void changeSecret(String secret) {
        this.clientSecret = secret;
    }
}

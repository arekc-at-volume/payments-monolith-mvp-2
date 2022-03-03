package com.volume.payments.shared.infrastructure.persistence;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;

@MappedSuperclass
@ToString
public class BaseNonKeyedVersionedEntity {
    @Version private Long version;

    protected BaseNonKeyedVersionedEntity() {}

    public Long getVersion() {
        return this.version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }
}

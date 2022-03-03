package com.volume.payments.shared.infrastructure.persistence;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;

@MappedSuperclass
@ToString
public class BaseKeyedVersionedEntity<PK extends Serializable> {
    @Id private PK id;
    @Version private Long version;

    protected BaseKeyedVersionedEntity() {}

    protected BaseKeyedVersionedEntity(PK id) {
        this.id = id;
    }

    public PK getId() {
        return this.id;
    }

    public Long getVersion() {
        return this.version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        BaseKeyedVersionedEntity other = (BaseKeyedVersionedEntity) obj;

        if (id == null) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        var prime = 31;
        return prime + ((id == null) ? 0 : id.hashCode());
    }
}


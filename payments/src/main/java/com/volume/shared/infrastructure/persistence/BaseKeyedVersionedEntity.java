package com.volume.shared.infrastructure.persistence;

import com.google.common.base.Preconditions;
import lombok.ToString;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.io.Serializable;

import static java.lang.String.format;

@MappedSuperclass
@ToString
public class BaseKeyedVersionedEntity<PK extends Serializable> {
    @Id private PK id;
    @Version private Long version;

    protected BaseKeyedVersionedEntity() {}

    protected BaseKeyedVersionedEntity(PK id) {
        // TODO: verify this message. It should never reach user so how to ensure it won't but we'll have it logged.
        // Anyway it should entirely go away when we switch to Kotlin.
        Preconditions.checkNotNull(id, format("Primary key of an entity of type %s cannot be null", this.getClass().getName()));
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


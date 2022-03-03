package com.volume.shared.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseKeyedVersionedEntityRepository<Entity extends BaseKeyedVersionedEntity<PK>, PK extends Serializable>
        extends JpaRepository<Entity, PK> {
}

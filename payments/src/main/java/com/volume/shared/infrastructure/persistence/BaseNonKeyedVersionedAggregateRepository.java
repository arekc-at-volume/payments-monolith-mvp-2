package com.volume.shared.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseNonKeyedVersionedAggregateRepository<Aggregate extends BaseNonKeyedVersionedAggregateRoot, PK extends Serializable>
        extends JpaRepository<Aggregate, PK> {
}

package com.volume.payments.shared.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseKeyedVersionedAggregateRepository<Aggregate extends BaseKeyedVersionedAggregateRoot<PK>, PK extends Serializable>
        extends JpaRepository<Aggregate, PK> {
}


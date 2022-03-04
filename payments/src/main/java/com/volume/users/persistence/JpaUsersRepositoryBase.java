package com.volume.users.persistence;

import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRepository;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRoot;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface JpaUsersRepositoryBase<Aggregate extends BaseKeyedVersionedAggregateRoot<UserId>> extends BaseKeyedVersionedAggregateRepository<Aggregate, UserId> {
}

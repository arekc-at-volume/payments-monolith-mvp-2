package com.volume.transfers.persistence;

import com.volume.shared.domain.types.TransferId;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedAggregateRepository;
import com.volume.transfers.TransferAggregate;

public interface JpaTransferAggregateRepository extends BaseKeyedVersionedAggregateRepository<TransferAggregate, TransferId> {
}

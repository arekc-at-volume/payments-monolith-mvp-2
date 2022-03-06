package com.volume.users.exceptions

import com.volume.shared.domain.DomainException
import com.volume.shared.domain.types.TransferId

class TransferNotFoundException : DomainException {
    constructor(message: String) : super(message)
    constructor(message: String, throwable: Throwable) : super(message, throwable)
    constructor(transferId: TransferId) : super("Transfer with id=${transferId.asString()} does not exist");
    constructor(transferId: TransferId, throwable: Throwable) : super("Transfer with id=${transferId.asString()} does not exist", throwable);

}
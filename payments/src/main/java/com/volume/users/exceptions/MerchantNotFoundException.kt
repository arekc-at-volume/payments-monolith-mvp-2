package com.volume.users.exceptions

import com.volume.shared.domain.DomainException
import com.volume.shared.domain.types.UserId

class MerchantNotFoundException : DomainException {
    constructor(message: String) : super(message)
    constructor(message: String, throwable: Throwable) : super(message, throwable)
    constructor(merchantId: UserId) : super("Merchant with id=${merchantId.value.toString()} does not exist");
    constructor(merchantId: UserId, throwable: Throwable) : super("Merchant with id=${merchantId.value.toString()} does not exist", throwable);
}


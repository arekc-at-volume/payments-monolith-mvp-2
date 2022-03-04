package com.volume.users.exceptions

import com.volume.shared.domain.DomainException
import com.volume.shared.domain.types.UserId

class ShopperNotFoundException : DomainException {
    constructor(message: String) : super(message)
    constructor(message: String, throwable: Throwable) : super(message, throwable)
    constructor(ShopperId: UserId) : super("Shopper with id=${ShopperId.value.toString()} does not exist");
    constructor(ShopperId: UserId, throwable: Throwable) : super("Shopper with id=${ShopperId.value.toString()} does not exist", throwable);
}
package com.volume.payments.shared.domain

open class DomainException: Exception {
    constructor(message: String) : super(message)
    constructor(message: String, throwable: Throwable) : super(message, throwable)
}


package com.example.aggregatedemojava.utilities.infrastructure.rest

import com.volume.payments.shared.domain.DomainException
import org.springframework.cloud.sleuth.Tracer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * TODO: I wonder if there is a way to make message property non-nullable
 */
data class RestErrorResponse(val status: HttpStatus, val message: String?, val traceId: String?) {
    companion object {
        fun fromException(status: HttpStatus, exception: DomainException, tracer: Tracer) : ResponseEntity<RestErrorResponse> {
            return ResponseEntity
                .status(status)
                .body(
                    RestErrorResponse(
                        status,
                        exception.message,
                        tracer.currentSpan()?.context()?.traceId())
                )
        }
    }
}


package com.volume.shared.infrastructure.rest;

import com.volume.shared.domain.DomainException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class RestErrorResponse {
    private final HttpStatus status;
    private final String message;
    private final String traceId;

    public static ResponseEntity<RestErrorResponse> fromException(HttpStatus status, DomainException exception, Tracer tracer) {
        return ResponseEntity.status(status).body(new RestErrorResponse(status, exception.getMessage(), tracer.currentSpan().context().traceId()));
    }
}

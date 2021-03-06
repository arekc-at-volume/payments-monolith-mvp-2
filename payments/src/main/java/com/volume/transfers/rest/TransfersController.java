package com.volume.transfers.rest;

import com.volume.shared.domain.AuthenticatedUser;
import com.volume.shared.domain.types.TransferId;
import com.volume.transfers.TransferAggregateService;
import com.volume.transfers.rest.dto.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@AllArgsConstructor
public class TransfersController {

    private final TransferAggregateService transferAggregateService;


    @PostMapping("/api/transfers")
    ResponseEntity<CreateTransferResponseDto> createTransfer(@RequestBody CreateTransferRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transferAggregateService.createNewTransfer(AuthenticatedUser.merchant(), requestDto));
    }

    @GetMapping("/api/transfers")
    ResponseEntity<List<TransferDto>> getAllTransfers() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(transferAggregateService.getAllTransfers());
    }

    @PostMapping("/api/transfers/{transferId}/startAuthorization")
    ResponseEntity<GeneratePaymentAuthorizationUrlResponseDto> startAuthorization(@RequestBody GeneratePaymentAuthorizationUrlRequestDto requestDto) {
        var responseDto = transferAggregateService.generateAuthorizationUrl(AuthenticatedUser.merchant(), requestDto);
        return ResponseEntity
                .status(HttpStatus.PERMANENT_REDIRECT)
                .location(URI.create(responseDto.getAuthorizationUrl()))
                .body(responseDto);
    }

    @GetMapping("/api/callback/")
    ResponseEntity callback(@RequestParam("one-time-token") String oneTimeTokenString, @RequestParam("transferId") String transferIdString) {
        var transferId = TransferId.Companion.fromString(transferIdString);

        var handleCallbackAndMakePayment = true;

        Object responseDto = null;

        if (handleCallbackAndMakePayment) {
            responseDto = transferAggregateService.handleAuthorizationCallbackAndMakePayment(
                    AuthenticatedUser.merchant(),
                    new HandleAuthorizationCallbackRequestDto(transferId, oneTimeTokenString));
        } else {
            responseDto = transferAggregateService.handleAuthorizationCallback(
                    AuthenticatedUser.merchant(),
                    new HandleAuthorizationCallbackRequestDto(transferId, oneTimeTokenString));
        }

        return ResponseEntity.ok().body(responseDto);
    }

    @PostMapping("/api/transfers/{transferId}/make-payment")
    ResponseEntity<MakePaymentResponseDto> makePayment(@PathVariable("transferId") String transferIdString, @RequestBody MakePaymentRequestDto requestDto) {
        var transferId = TransferId.Companion.fromString(transferIdString);
        MakePaymentResponseDto makePaymentResponseDto = transferAggregateService.makePayment(
                AuthenticatedUser.merchant(),
                requestDto);

        return ResponseEntity.ok().body(makePaymentResponseDto);
    }

    @PostMapping("/api/transfers/start-transfer-flow")
    ResponseEntity<RunPaymentFlowResponseDto> runPaymentFlow(@RequestBody RunPaymentFlowRequestDto requestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transferAggregateService.runPaymentFlow(AuthenticatedUser.merchant(), requestDto));
    }
}


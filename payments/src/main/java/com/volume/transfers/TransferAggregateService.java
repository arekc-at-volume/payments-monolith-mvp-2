package com.volume.transfers;

import com.volume.shared.domain.messages.GeneratePaymentAuthorizationUrlCommand;
import com.volume.transfers.persistence.JpaTransferAggregateRepository;
import com.volume.transfers.rest.dto.*;
import com.volume.shared.domain.AuthenticatedUser;
import com.volume.users.MerchantAggregate;
import com.volume.users.ShopperAggregate;
import com.volume.users.exceptions.MerchantNotFoundException;
import com.volume.users.exceptions.ShopperNotFoundException;
import com.volume.users.exceptions.TransferNotFoundException;
import com.volume.users.persistence.JpaMerchantsRepository;
import com.volume.users.persistence.JpaShoppersRepository;
import com.volume.yapily.YapilyClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TransferAggregateService {

    private final JpaTransferAggregateRepository transferRepository;
    private final JpaShoppersRepository shopperRepository;
    private final JpaMerchantsRepository merchantRepository;
    private final YapilyClient yapilyClient;

    /**
     * We should discuss how we want to have such constraints handled. I think it should be within aggregate command
     * handlers until it uses types/repositories/services from within same bounded context.
     * <p>
     * In this case we should build separate shopper repository in transfer context based on shopper events. (TODO)
     * The same should happen to Merchant payee details (TODO)
     */
    public CreateTransferResponseDto createNewTransfer(AuthenticatedUser callingUser, CreateTransferRequestDto requestDto) {
        // constraint 1 : shopper must exist
        ShopperAggregate shopperAggregate = shopperRepository.findById(requestDto.getShopperId())
                .orElseThrow(() -> new ShopperNotFoundException(requestDto.getShopperId()));

        // constraint 2 : merchant must exist
        MerchantAggregate merchantAggregate = merchantRepository.findById(requestDto.getMerchantId())
                .orElseThrow(() -> new MerchantNotFoundException(requestDto.getMerchantId()));

        // constraint 3 : merchant must have payee details properly configured
        // TODO

        TransferPayeeDetailsDto payeeDetailsDto = TransferPayeeDetailsDto.from(merchantAggregate.getMerchantPayeeDetails().toDto());

        TransferAggregate transferAggregate = TransferAggregate.create(
                callingUser,
                requestDto.toCommand(
                        shopperAggregate.getYapilyApplicationUserId(),
                        shopperAggregate.getYapilyUserId(),
                        payeeDetailsDto
                ),
                transferRepository
        );

        return CreateTransferResponseDto.fromAggregate(transferAggregate);
    }

    public GeneratePaymentAuthorizationUrlResponseDto generateAuthorizationUrl(AuthenticatedUser callingUser, GeneratePaymentAuthorizationUrlRequestDto requestDto) {
        // constraint 1: transfer must exist and be in status = TODO
        TransferAggregate transferAggregateBefore = transferRepository.findById(requestDto.getTransferId())
                .orElseThrow(() -> new TransferNotFoundException(requestDto.getTransferId()));
        // TODO: validate transfer status

        GeneratePaymentAuthorizationUrlCommand generatePaymentAuthorizationUrlCommand = requestDto.toCommand();
        TransferAggregate transferAggregateAfter =
                transferAggregateBefore.handle(generatePaymentAuthorizationUrlCommand, transferRepository, yapilyClient);

        return GeneratePaymentAuthorizationUrlResponseDto.fromAggregate(transferAggregateAfter);
    }


}

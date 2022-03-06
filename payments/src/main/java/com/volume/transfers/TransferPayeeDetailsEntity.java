package com.volume.transfers;

import com.volume.shared.domain.types.TransferId;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedEntity;
import com.volume.users.AccountIdentificationVO;
import com.volume.users.PostalAddressVO;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
class TransferPayeeDetailsEntity extends BaseKeyedVersionedEntity<TransferId> {
    private String accountHolderName;
    @Embedded
    private PostalAddressVO postalAddress;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "account_id_1_type")),
            @AttributeOverride(name = "number", column = @Column(name = "account_id_1_number")),
    })
    private AccountIdentificationVO accountIdentification1;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "account_id_2_type")),
            @AttributeOverride(name = "number", column = @Column(name = "account_id_2_number")),
    })
    private AccountIdentificationVO accountIdentification2;
    @OneToOne
    private TransferAggregate transfer;

    protected TransferPayeeDetailsEntity() {
        super();
    }

    public TransferPayeeDetailsEntity(TransferId id, String accountHolderName, PostalAddressVO postalAddress, AccountIdentificationVO accountIdentification1, AccountIdentificationVO accountIdentification2) {
        super(id);
        this.accountHolderName = accountHolderName;
        this.postalAddress = postalAddress;
        this.accountIdentification1 = accountIdentification1;
        this.accountIdentification2 = accountIdentification2;
    }

    public static TransferPayeeDetailsEntity forTest(TransferId transferId) {
        return new TransferPayeeDetailsEntity(
                transferId,
                "test account holder name",
                PostalAddressVO.testDomesticPaymentUKPayeeAddress(),
                AccountIdentificationVO.testAccountNumber(),
                AccountIdentificationVO.testSortCode()
        );
    }

    public void setTransfer(TransferAggregate transferAggregate) {
        this.transfer = transferAggregate;
    }
}

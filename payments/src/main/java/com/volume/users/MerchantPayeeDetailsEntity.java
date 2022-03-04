package com.volume.users;

import com.volume.shared.domain.types.UserId;
import com.volume.shared.infrastructure.persistence.BaseKeyedVersionedEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * TODO: Ultimately I don't think primary key will be UserId. I leave if like that for now.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class MerchantPayeeDetailsEntity extends BaseKeyedVersionedEntity<UserId> {
    @OneToOne
    private MerchantAggregate merchant;
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

    public MerchantPayeeDetailsEntity(
            UserId id,
            MerchantAggregate merchant,
            String accountHolderName,
            PostalAddressVO postalAddress,
            AccountIdentificationVO accountIdentification1,
            AccountIdentificationVO accountIdentification2
    ) {
        super(id);
        this.merchant = merchant;
        this.accountHolderName = accountHolderName;
        this.postalAddress = postalAddress;
        this.accountIdentification1 = accountIdentification1;
        this.accountIdentification2 = accountIdentification2;
    }

    public void setMerchant(MerchantAggregate merchant) {
        this.merchant = merchant;
    }
}

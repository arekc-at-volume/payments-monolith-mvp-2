package com.volume.yapily.client

import com.volume.yapily.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import yapily.sdk.AccountIdentification
import yapily.sdk.ApplicationUser
import yapily.sdk.Institution
import yapily.sdk.PaymentAuthorisationRequestResponse
import java.math.BigDecimal


data class AccountIdentificationSortCode(val sortCode: String) : YapilyAccountIdentification {
    init {
        // validate here
    }

    override fun toYapily(): AccountIdentification {
        var result = AccountIdentification()
        result.type = AccountIdentification.TypeEnum.SORT_CODE
        result.identification = this.sortCode;
        return result;
    }

    override fun toDbString(): String {
        return "${AccountIdentification.TypeEnum.SORT_CODE}:$sortCode"
    }
}

data class AccountIdentificationAccountNumber(val accountNumber: String) : YapilyAccountIdentification {
    init {
        // validate here
    }

    override fun toYapily(): AccountIdentification {
        var result = AccountIdentification()
        result.type = AccountIdentification.TypeEnum.SORT_CODE
        result.identification = this.accountNumber;
        return result;
    }

    override fun toDbString(): String {
        return "${AccountIdentification.TypeEnum.ACCOUNT_NUMBER}:$accountNumber"
    }
}

/**
 * This test shows
 */
class YapilyTestInstitutionsForAuthorizationFlowType {

    @Test
    fun testInstitutions() {
        var yapily = YapilyClientImpl();
        yapily.getInstitutions()
            .filter { !institutionSupportsConnectedDirectFlowWithCallback(it) }
            .forEach { println(it.fullName) }
    }

    fun institutionSupportsConnectedDirectFlowWithCallback(institution: Institution): Boolean {
        if (
            institution.features.contains(Institution.FeaturesEnum.INITIATE_DOMESTIC_SINGLE_PAYMENT)
            && !institution.features.contains(Institution.FeaturesEnum.INITIATE_PRE_AUTHORISATION)
            && !institution.features.contains(Institution.FeaturesEnum.INITIATE_EMBEDDED_DOMESTIC_SINGLE_PAYMENT)
        ) {
            return true;
        }

        return false;
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class YapilyClientImplTest {

    val client = YapilyClientImpl()
    val INSTITUTION = InstitutionId("modelo-sandbox")
    lateinit var existingClient: ApplicationUser

    @BeforeAll
    fun setup() {
        client.deleteAllUsers { user -> println("Deleting user ${user.applicationUserId} from Yapily sandbox") }
        existingClient =
            client.createApplicationUser(YapilyApplicationUserId("test_user@test.com"), YapilyReferenceUserId.random())
    }

    @Test
    fun getInstitutions() {
        val institutions = client.getInstitutions()
        Assertions.assertThat(institutions).isNotEmpty
    }

    @Test
    fun createApplicationUser() {
        var applicationUserId = YapilyApplicationUserId("arek1@test.com")
        var referenceUserId = YapilyReferenceUserId.random()
        val applicationUser =
            client.createApplicationUser(applicationUserId, referenceUserId)

        Assertions.assertThat(applicationUser).isNotNull
        Assertions.assertThat(applicationUser.applicationUserId).isEqualTo(applicationUserId.value)
        Assertions.assertThat(applicationUser.referenceId).isEqualTo(referenceUserId.value.toString())
        Assertions.assertThat(applicationUser.uuid).isNotEmpty()
        Assertions.assertThat(applicationUser.institutionConsents).isEmpty()
    }

    @Test
    fun generateAuthorizationUrl() {
        val applicationUserId = YapilyApplicationUserId("arek1@test.com")
        val idempotencyId = PaymentIdempotencyId.random()
        val paymentRequest = client.createPaymentRequest(
            BigDecimal.valueOf(10),
            "test user",
            PaymentIdempotencyId.random(),
            "Test payment",
            listOf(
                AccountIdentificationFactory.sortCode("700001"),
                AccountIdentificationFactory.accountNumber("70000005")
            )
        )

        val paymentAuthorizationRequestResponse = client.generateAuthorizationUrl(
            YapilyApplicationUserId(existingClient.applicationUserId),
            INSTITUTION,
            paymentRequest,
            "https://localhost:8080/api/callback/"
        )

        Assertions.assertThat(paymentAuthorizationRequestResponse).isNotNull
        Assertions.assertThat(paymentAuthorizationRequestResponse.applicationUserId)
            .isEqualTo(existingClient.applicationUserId)
        Assertions.assertThat(paymentAuthorizationRequestResponse.userUuid).isEqualTo(existingClient.uuid)
        Assertions.assertThat(paymentAuthorizationRequestResponse.institutionId).isEqualTo(INSTITUTION.value)
        Assertions.assertThat(paymentAuthorizationRequestResponse.status)
            .isEqualTo(PaymentAuthorisationRequestResponse.StatusEnum.AWAITING_AUTHORIZATION)
        Assertions.assertThat(paymentAuthorizationRequestResponse.authorisationUrl).isNotBlank()
        Assertions.assertThat(paymentAuthorizationRequestResponse.qrCodeUrl).isNotBlank()
        // TODO: read more about this field
        Assertions.assertThat(paymentAuthorizationRequestResponse.status).isNotNull()
        // TODO: read more about this field
        Assertions.assertThat(paymentAuthorizationRequestResponse.institutionConsentId).isNotNull()
    }


}
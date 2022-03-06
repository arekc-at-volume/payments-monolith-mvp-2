package com.volume.yapily

import org.springframework.stereotype.Service
import yapily.ApiClient
import yapily.auth.HttpBasicAuth
import yapily.sdk.*
import java.math.BigDecimal
import java.util.function.Consumer

@Service
class YapilyClientImpl : YapilyClient {
    private val APPLICATION_ID: String = "fac901bb-72cb-484a-bdf8-fdb083961edc";
    private val APPLICATION_SECRET: String = "a0052cd6-0249-4763-9621-b6cf041612e4";
    private val API_VERSION = "1.0"


    val apiClient = ApiClient()
    val institutionsApi: InstitutionsApi
    val applicationUserApi: ApplicationUsersApi
    val paymentsApi: PaymentsApi
    val consentApi: ConsentsApi

    init {
        var auth = apiClient.getAuthentication("basicAuth") as HttpBasicAuth
        auth.username = APPLICATION_ID
        auth.password = APPLICATION_SECRET

        institutionsApi = InstitutionsApi(apiClient)
        applicationUserApi = ApplicationUsersApi(apiClient)
        paymentsApi = PaymentsApi(apiClient)
        consentApi = ConsentsApi(apiClient)
    }

    override fun getInstitutions(): List<Institution> {
        return institutionsApi.getInstitutionsUsingGET(API_VERSION).data
    }

    override fun createApplicationUser(applicationUserId: YapilyApplicationUserId, referenceId: YapilyReferenceUserId): ApplicationUser {
        var newUser = NewApplicationUser()
        newUser.applicationUserId = applicationUserId.value
        newUser.referenceId = referenceId.value.toString()

        return applicationUserApi.addUserUsingPOST(newUser, API_VERSION)
    }

    override fun deleteAllUsers(beforeDeleting: Consumer<ApplicationUser>) {
        applicationUserApi.getUsersUsingGET(APPLICATION_ID, listOf())
            .forEach {
                beforeDeleting.accept(it)
                applicationUserApi.deleteUserUsingDELETE(it.uuid, API_VERSION)
            }
    }

    override fun deleteAllUsersExceptTestUser(beforeDeleting: Consumer<ApplicationUser>) {
        applicationUserApi.getUsersUsingGET(APPLICATION_ID, listOf("test_user@test.com"))
            .forEach {
                beforeDeleting.accept(it)
                applicationUserApi.deleteUserUsingDELETE(it.uuid, API_VERSION)
            }
    }

    override fun deleteAllTestsUsers() {
        TODO("Not yet implemented")
    }

    override fun generateAuthorizationUrl(
        userApplicationId: YapilyApplicationUserId,
        institutionId: YapilyInstitutionId,
        paymentRequest: PaymentRequest,
        oneTimeToken: Boolean,
        callback: String
    ): PaymentAuthorisationRequestResponse {
        var authorizationRequest = PaymentAuthorisationRequest()
        authorizationRequest.institutionId = institutionId.value
        authorizationRequest.applicationUserId = userApplicationId.value
        authorizationRequest.callback = callback
        authorizationRequest.paymentRequest = paymentRequest
        authorizationRequest.oneTimeToken = oneTimeToken

        return paymentsApi.createPaymentAuthorisationUsingPOST(
            authorizationRequest, API_VERSION, null, null, null
        ).data
    }

    override fun exchangeOneTimeToken(oneTimeToken: String): Consent {
        var request = OneTimeTokenRequest()
        request.oneTimeToken = oneTimeToken;
        return consentApi.getConsentBySingleAccessConsentUsingPOST(request, API_VERSION)
    }

    override fun makePayment(consentToken: String, paymentRequest: PaymentRequest): ApiResponseOfPaymentResponse {
        return paymentsApi.createPaymentUsingPOST(consentToken, paymentRequest, API_VERSION, null, null, null)
    }

    override fun createPaymentRequest(
        transferredAmount: BigDecimal,
        transferredCurrency: String,
        payeeName: String,
        paymentIdempotencyId: YapilyPaymentIdempotencyId,
        paymentDescription: String,
        payeeAccountIdentifications: List<YapilyAccountIdentification>,
        readRefundAccount: Boolean
    ): PaymentRequest {

        var amount = Amount()
        amount.amount = transferredAmount
        amount.currency = transferredCurrency

        var payee = Payee()
        payee.name = payeeName

        payee.accountIdentifications = payeeAccountIdentifications.map { it.toYapily() }.toList()

        val paymentRequest = PaymentRequest()
        paymentRequest.paymentIdempotencyId = paymentIdempotencyId.value
        paymentRequest.payee = payee
        paymentRequest.type = PaymentRequest.TypeEnum.DOMESTIC_PAYMENT
        paymentRequest.reference = paymentDescription
        paymentRequest.amount = amount
        paymentRequest.readRefundAccount = readRefundAccount

        return paymentRequest
    }
}
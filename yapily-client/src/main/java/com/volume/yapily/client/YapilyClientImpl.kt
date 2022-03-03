package com.volume.yapily

import org.springframework.stereotype.Service
import yapily.ApiClient
import yapily.auth.HttpBasicAuth
import yapily.sdk.*
import java.math.BigDecimal
import java.util.function.Consumer

@Service
class YapilyClientImpl : YapilyClient {
    private val APPLICATION_ID: String = "5304fabb-8d11-43bc-8198-bcc97cbd226f";
    private val APPLICATION_SECRET: String = "3603af71-22e9-48b5-afcc-691d80958d2c";
    private val API_VERSION = "1.0"


    val apiClient = ApiClient()
    val institutionsApi: InstitutionsApi
    val applicationUserApi: ApplicationUsersApi
    val paymentsApi: PaymentsApi

    init {
        var auth = apiClient.getAuthentication("basicAuth") as HttpBasicAuth
        auth.username = APPLICATION_ID
        auth.password = APPLICATION_SECRET

        institutionsApi = InstitutionsApi(apiClient)
        applicationUserApi = ApplicationUsersApi(apiClient)
        paymentsApi = PaymentsApi(apiClient)
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
        institutionId: InstitutionId,
        paymentRequest: PaymentRequest,
        callback: String
    ): PaymentAuthorisationRequestResponse {
        var authorizationRequest = PaymentAuthorisationRequest()
        authorizationRequest.institutionId = institutionId.value
        authorizationRequest.applicationUserId = userApplicationId.value
        authorizationRequest.callback = callback
        authorizationRequest.paymentRequest = paymentRequest

        return paymentsApi.createPaymentAuthorisationUsingPOST(
            authorizationRequest, API_VERSION, null, null, null
        ).data
    }

    override fun makePayment(consentToken: String, paymentRequest: PaymentRequest): ApiResponseOfPaymentResponse {
        return paymentsApi.createPaymentUsingPOST(consentToken, paymentRequest, API_VERSION, null, null, null)
    }

    override fun createPaymentRequest(
        transferredAmount: BigDecimal,
        payeeName: String,
        paymentIdempotencyId: PaymentIdempotencyId,
        paymentDescription: String,
        payeeAccountIdentifications: List<YapilyAccountIdentification>
    ): PaymentRequest {

        var amount = Amount()
        amount.amount = transferredAmount
        amount.currency = "GBP"

        var payee = Payee()
        payee.name = payeeName

        payee.accountIdentifications = payeeAccountIdentifications.map { it.toYapily() }.toList()

        val paymentRequest = PaymentRequest()
        paymentRequest.paymentIdempotencyId = paymentIdempotencyId.value
        paymentRequest.payee = payee
        paymentRequest.type = PaymentRequest.TypeEnum.DOMESTIC_PAYMENT
        paymentRequest.reference = paymentDescription
        paymentRequest.amount = amount

        return paymentRequest
    }
}
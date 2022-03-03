package com.volume.yapily

import yapily.ApiException
import yapily.sdk.*
import java.math.BigDecimal
import java.util.*
import java.util.function.Consumer

/**
 * The point of this interface is to:
 * 1. Make it harder to make any mistakes
 * 2. Have one place to describe all that we know/think is true about that api
 */
interface YapilyClient {
    companion object {
        val DEFAULT_CALLBACK = "https://display-parameters.com/"
    }

    /**
     * Gets all Institutions registered for an application
     *
     * @return List of Institutions
     */
    fun getInstitutions(): List<Institution>

    /**
     * Creates new user for current application.
     *
     * @param applicationUserId user friendly id (email? think about it, somehow differently generated id)
     * @param referenceId I assume that this is sth like our id injected into yapily (TO BE CONFIRMED)
     * @throws ApiException
     */
    fun createApplicationUser(applicationUserId: YapilyApplicationUserId, referenceId: YapilyReferenceUserId): ApplicationUser

    fun deleteAllUsers(beforeDeleting: Consumer<ApplicationUser>)
    fun deleteAllUsersExceptTestUser(beforeDeleting: Consumer<ApplicationUser>)

    fun deleteAllTestsUsers()

    /**
     * Makes a request to create authorization-url for CREATE_DOMESTIC_SINGLE_PAYMENT (institution feature)
     *
     *
     * Described:
     * - https://docs.yapily.com/guides/payments/#2-authorisation
     * - https://docs.yapily.com/api/#create-payment-authorisation
     *
     *
     * Most important parts of response are:
     * - userUuid (IMPORTANT TODO: think what should we do with "user" term in our application)
     * - authorizationUrl (deep link to be passed to user. it should redirect him to bank app for authentication/authorization of consent)
     * - qrCode (in the future if we want to use it from web payment?)
     *
     * @param userApplicationId
     * @param institutionId
     * @param callback
     * @param paymentRequest
     * @return
     */
    fun generateAuthorizationUrl(
        userApplicationId: YapilyApplicationUserId,
        institutionId: InstitutionId,
        paymentRequest: PaymentRequest,
        callback: String = DEFAULT_CALLBACK
    ): PaymentAuthorisationRequestResponse

    /**
     * Once user authorized CREATE_DOMESTIC_SINGLE_PAYMENT we can create the payment.
     *
     *
     * Creates the payment using consentToken retrieves from the institution in previous step.
     * It MUST provide EXACTLY THE SAME PaymentRequest as was provided while generating authorization-url!!!
     *
     * @param consentToken
     * @param paymentRequest
     * @return
     */
    fun makePayment(consentToken: String, paymentRequest: PaymentRequest): ApiResponseOfPaymentResponse

    /**
     * Helper method to create fixed PaymentRequest for testing
     *
     * @param transferredAmount
     * @param payeeName
     * @return
     * @throws ApiException
     */
    fun createPaymentRequest(
        transferredAmount: BigDecimal,
        payeeName: String,
        paymentIdempotencyId: PaymentIdempotencyId,
        // translates to paymentRequest.reference (I think it is a payment description)
        paymentDescription: String,
        payeeAccountIdentifications: List<YapilyAccountIdentification>
    ): PaymentRequest
}


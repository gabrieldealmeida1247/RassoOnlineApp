package com.example.rassoonlineapp.API

import com.example.rassoonlineapp.Model.CustomerModel
import com.example.rassoonlineapp.Model.PaymentIntentModel
import com.example.rassoonlineapp.Utils.Stripe.SECRET_KEY
import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterface {

    @Headers("Authorization: Bearer $SECRET_KEY")
    @POST("v1/customers")
    suspend fun getCustomer() : Response<CustomerModel>

    @Headers("Authorization: Bearer $SECRET_KEY", "Stripe-Version: 2024-04-10")
    @POST("v1/ephemeral_keys")
    suspend fun getEphemeralKey(
        @Query("customer") customer: String
    ) : Response<CustomerModel>


    @Headers("Authorization: Bearer $SECRET_KEY")
    @POST("v1/payment_intents")
    suspend fun getPaymentIntent(
        @Query("customer") customer: String,
        @Query("amount") amount: String="1099",
        @Query("currency") currency: String="eur",
        @Query("automatic_payment_methods[enabled]") automatePay: Boolean = true,
    ) : Response<PaymentIntentModel>


}
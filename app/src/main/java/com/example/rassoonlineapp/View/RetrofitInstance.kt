package com.example.rassoonlineapp.View

import com.example.rassoonlineapp.Constants.Constants
import com.example.rassoonlineapp.Interface.NotificationApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {  companion object{
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: NotificationApi by lazy {
        retrofit.create(NotificationApi::class.java)
    }
}


}
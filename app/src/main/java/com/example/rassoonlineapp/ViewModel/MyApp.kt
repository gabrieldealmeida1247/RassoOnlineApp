package com.example.rassoonlineapp.ViewModel


import android.app.Application
import com.stripe.android.PaymentConfiguration

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51PBehgAVtFNkTOAvms6hDjOn2gDHcNIG7hWMctpVeZktYHRqzQFrfOSZZ5mRpmuuw9h94gD22YQnJwO451prSenG00OhSmZ88W"
        )
    }
}
package com.example.rassoonlineapp.Admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.rassoonlineapp.R
import com.example.rassoonlineapp.View.SignInActivity
import com.google.firebase.auth.FirebaseAuth

class BannedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banned)

        val backLogin = findViewById<ImageView>(R.id.back_login)
        backLogin.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            true
        }


    }
}

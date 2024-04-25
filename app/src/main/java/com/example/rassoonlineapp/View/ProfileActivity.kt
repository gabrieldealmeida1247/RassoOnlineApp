package com.example.rassoonlineapp.View

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.rassoonlineapp.Fragments.ProfileFragment
import com.example.rassoonlineapp.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Adicione o ProfileFragment ao contêiner de fragmentos
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProfileFragment())
            .commit()


        findViewById<ImageView>(R.id.arrow_back).setOnClickListener {
        onBackPressed()
        }
    }
}
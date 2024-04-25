package com.example.rassoonlineapp.View

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rassoonlineapp.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SignInActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var register: TextView
    private lateinit var login: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize views
        email = findViewById(R.id.emailET)
        password = findViewById(R.id.passET)
        register = findViewById(R.id.signupTv)
        login = findViewById(R.id.btnLogin)

        val forgetTv = findViewById<TextView>(R.id.forgetTv).setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }

        login.setOnClickListener {
            val sEmail = email.text.toString().trim()
            val sPassword = password.text.toString().trim()


            when {
                TextUtils.isEmpty(sEmail) -> Toast.makeText(
                    this,
                    "Email is required",
                    Toast.LENGTH_LONG
                ).show()

                TextUtils.isEmpty(sPassword) -> Toast.makeText(
                    this,
                    "Password is required",
                    Toast.LENGTH_LONG
                ).show()

            else ->{
            //
            auth.signInWithEmailAndPassword(sEmail, sPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val verification = auth.currentUser?.isEmailVerified
                        if (verification == true) {
                            Log.d(TAG, "signInWithEmail:success")
                            updateUI()
                        } else {
                            Toast.makeText(this, "Por favor verifique o seu email!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        // Verificar se task.exception é null
                        if (task.exception == null) {
                            Toast.makeText(this, "Authentication failed: Credenciais inválidas.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }

                    }
                }

            }
        }
    }

        register.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateUI() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI()
        }
    }
}

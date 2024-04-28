package com.example.rassoonlineapp.View

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rassoonlineapp.Admin.AdminActivity
import com.example.rassoonlineapp.Admin.BannedActivity
import com.example.rassoonlineapp.Admin.model.BannedUser
import com.example.rassoonlineapp.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class SignInActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var register: TextView
    private lateinit var login: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize Firebase Auth
        auth = Firebase.auth
        // Initialize Firebase Database
        database = Firebase.database.reference
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
                // Verifica se o email e a senha correspondem aos valores desejados
                if (sEmail == "admin@gmail.com" && sPassword == "123456") {
                    // Se corresponderem, direciona para a tela de administração
                    val intent = Intent(this, AdminActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    auth.signInWithEmailAndPassword(sEmail, sPassword)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val verification = auth.currentUser?.isEmailVerified
                                if (verification == true) {
                                    Log.d(TAG, "signInWithEmail:success")
                                    checkBanStatus(auth.currentUser!!)
                                    //   updateUI()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Por favor verifique o seu email!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Log.w(TAG, "signInWithEmail:failure", task.exception)
                                // Verificar se task.exception é null
                                if (task.exception == null) {
                                    Toast.makeText(
                                        this,
                                        "Authentication failed: Credenciais inválidas.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Authentication failed.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

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
            checkBanStatus(currentUser)
        }
    }



    private fun checkBanStatus(user: FirebaseUser) {
        val bannedUsersRef = database.child("banned_users")
        bannedUsersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (banSnapshot in dataSnapshot.children) {
                    val bannedUser = banSnapshot.getValue(BannedUser::class.java)
                    if (bannedUser != null && bannedUser.userId == user.uid) {
                        // User is banned, redirect to ban screen
                        redirectToBanScreen()
                        return
                    }
                }
                // User is not banned, proceed with app
                // Redirect to main activity or wherever you want to go
                redirectToMainActivity()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(applicationContext, "Erro ao verificar status de banimento", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun redirectToBanScreen() {
        val intent = Intent(this, BannedActivity::class.java)
        startActivity(intent)
        finish() // Prevent user from going back to login screen
    }

    private fun redirectToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "SignInActivity"
    }

}

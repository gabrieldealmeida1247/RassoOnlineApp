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
                                    // Atualiza o status de autenticação
                                    updateUserAuthenticationStatus(true)
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
        // Verifica se os campos loggedInCount e loggedOutCount existem na base de dados
        checkUserCountFields()
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


    private fun updateUserAuthenticationStatus(isLoggedIn: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.child("UserCount").child(userId)

        // Define o valor com base no estado de autenticação
        userRef.child("isLoggedIn").setValue(isLoggedIn)

        // Atualiza a contagem de usuários logados/deslogados
        updateLoggedInUserCount(isLoggedIn)
    }

    private fun updateLoggedInUserCount(isLoggedIn: Boolean) {
        val userCountRef = database.child("UserCount")
        userCountRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var loggedInCount = dataSnapshot.child("loggedInCount").getValue(Int::class.java) ?: 0
                    var loggedOutCount = dataSnapshot.child("loggedOutCount").getValue(Int::class.java) ?: 0

                    // Verifica se o usuário está fazendo login
                    if (isLoggedIn) {
                        loggedInCount++
                        // Verifica se loggedOutCount é maior que zero antes de decrementar
                        if (loggedOutCount > 0) {
                            loggedOutCount--
                        }
                    } else {
                        // Se o usuário está fazendo logout, incrementa a contagem de usuários deslogados
                        loggedOutCount++
                    }

                    // Atualiza a contagem na base de dados
                    userCountRef.child("loggedInCount").setValue(loggedInCount)
                    userCountRef.child("loggedOutCount").setValue(loggedOutCount)
                } else {
                    // Se os dados não existirem, inicializa a contagem
                    if (isLoggedIn) {
                        userCountRef.child("loggedInCount").setValue(1)
                        userCountRef.child("loggedOutCount").setValue(0)
                    } else {
                        userCountRef.child("loggedInCount").setValue(0)
                        userCountRef.child("loggedOutCount").setValue(1)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
                Toast.makeText(applicationContext, "Erro ao atualizar contagem de usuários", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun checkUserCountFields() {
        // Verifica se os campos loggedInCount e loggedOutCount existem na base de dados
        database.child("UserCount").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userCountRef = database.child("UserCount")
                if (!dataSnapshot.child("loggedInCount").exists()) {
                    userCountRef.child("loggedInCount").setValue(0)
                }
                if (!dataSnapshot.child("loggedOutCount").exists()) {
                    userCountRef.child("loggedOutCount").setValue(0)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
                Toast.makeText(applicationContext, "Erro ao verificar contagem de usuários", Toast.LENGTH_SHORT).show()
            }
        })
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

package com.example.rassoonlineapp.View

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rassoonlineapp.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Suppress("DEPRECATION")
class SignupActivity : AppCompatActivity() {

    private lateinit var fullName: EditText
    private lateinit var userName: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var btnRegister: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize views
        fullName = findViewById(R.id.nameET)
        userName = findViewById(R.id.userName)
        email = findViewById(R.id.emailET)
        password = findViewById(R.id.passET)
        btnRegister = findViewById(R.id.btnNext)

        btnRegister = findViewById(R.id.btnNext)

        auth = Firebase.auth

        val btnNext = findViewById<Button>(R.id.btnProximo).setOnClickListener {
            updateUI()
        }

        btnRegister.setOnClickListener {

            val sEmail = email.text.toString().trim()
            val sPassword = password.text.toString().trim()
            val sFullName = fullName.text.toString()
            val sUserName = userName.text.toString()

            when {
                TextUtils.isEmpty(sFullName) -> Toast.makeText(
                    this,
                    "full name is required",
                    Toast.LENGTH_LONG
                ).show()

                TextUtils.isEmpty(sUserName) -> Toast.makeText(
                    this,
                    "User Name is required",
                    Toast.LENGTH_LONG
                ).show()

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
/*
                else -> {
                    auth.createUserWithEmailAndPassword(sEmail, sPassword)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information

                                auth.currentUser?.sendEmailVerification()
                                    ?.addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "Por favor verifique o seu email!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Chame saveUserInfo() passando os valores dos EditText
                                        val progressDialog = ProgressDialog(this)
                                        progressDialog.setTitle("Saving Information")
                                        progressDialog.setMessage("Aguarde enquanto criamos a sua conta...")
                                        progressDialog.setCanceledOnTouchOutside(false)
                                        progressDialog.show()

                                        saveUserInfo(sFullName, sUserName, sEmail, progressDialog)
                                    }
                                    ?.addOnFailureListener {
                                        Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT)
                                            .show()
                                    }

                                Log.d(TAG, "createUserWithEmail:success")
                                val user = auth.currentUser

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                Toast.makeText(
                                    baseContext,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT,
                                ).show()

                            }
                        }
                }

 */
                else -> {
                    // Verificar se o nome de usuário já está em uso
                    val userRef = FirebaseDatabase.getInstance().reference.child("Users")
                    userRef.orderByChild("username").equalTo(sUserName).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Nome de usuário já está em uso
                                Toast.makeText(this@SignupActivity, "Nome de usuário já está em uso", Toast.LENGTH_LONG).show()
                            } else {
                                // Nome de usuário disponível, criar conta
                                auth.createUserWithEmailAndPassword(sEmail, sPassword)
                                    .addOnCompleteListener(this@SignupActivity) { task ->
                                        if (task.isSuccessful) {
                                            // Conta criada com sucesso
                                            val progressDialog = ProgressDialog(this@SignupActivity)
                                            progressDialog.setTitle("Saving Information")
                                            progressDialog.setMessage("Aguarde enquanto criamos a sua conta...")
                                            progressDialog.setCanceledOnTouchOutside(false)
                                            progressDialog.show()

                                            saveUserInfo(sFullName, sUserName, sEmail, progressDialog)
                                        } else {
                                            // Falha ao criar conta
                                            Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                            Toast.makeText(
                                                baseContext,
                                                "Authentication failed.",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                    }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle errors
                        }
                    })
                }
            }
        }

    }


    private fun saveUserInfo(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName
        userMap["username"] = userName
        userMap["email"] = email
        userMap["description"] = ""
        userMap["especialidade"] = ""
        userMap["bio"] = "hello world"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/rasso-online.appspot.com/o/Default%20images%2Fprofile.png?alt=media&token=e2ce3d6b-7364-467d-ba18-9624cc671a34"
        userRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener{task ->
                if (task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this@SignupActivity, "A conta foi criada com sucesso", Toast.LENGTH_LONG).show()
                } else{
                    val message = task.exception?.message ?: "Ocorreu um erro ao criar a conta"
                    Toast.makeText(this@SignupActivity, message, Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                }
            }

    }

    private fun updateUI() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

}
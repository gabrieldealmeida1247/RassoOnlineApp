    package com.example.rassoonlineapp

    import android.app.ProgressDialog
    import android.content.Intent
    import android.os.Bundle
    import android.text.TextUtils
    import android.widget.Button
    import android.widget.EditText
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import com.example.rassoonlineapp.Admin.AdminActivity
    import com.google.firebase.auth.FirebaseAuth


    @Suppress("DEPRECATION")
    class SigninActivity : AppCompatActivity() {
        private var progressDialog: ProgressDialog? = null // Declare aqui
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_signin)

            progressDialog = ProgressDialog(this@SigninActivity) // Inicialize aqui

            findViewById<Button>(R.id.signup_link_btn).setOnClickListener {
                startActivity(Intent(this, SignUpActivity::class.java))
            }
            findViewById<Button>(R.id.login_btn).setOnClickListener {
                loginUser()
            }

        }


        private fun loginUser() {
            val email = findViewById<EditText>(R.id.email_login).text.toString()
            val password = findViewById<EditText>(R.id.password_login).text.toString()

            when {
                TextUtils.isEmpty(email) -> Toast.makeText(
                    this,
                    "Email is required",
                    Toast.LENGTH_LONG
                ).show()

                TextUtils.isEmpty(password) -> Toast.makeText(
                    this,
                    "Password is required",
                    Toast.LENGTH_LONG
                ).show()

                else -> {

                    progressDialog!!.setTitle("Login")
                    progressDialog!!.setMessage("Please wait, this may take a while...")
                    progressDialog!!.setCanceledOnTouchOutside(false)
                    progressDialog!!.show()

                    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                    if (email == "admin@gmail.com" && password == "admin@") {
                        // Se email e senha correspondem aos da conta de admin
                        progressDialog!!.dismiss()

                        val intent = Intent(this@SigninActivity, AdminActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()

                    } else {
                        // Se não é a conta de admin, faz login normalmente
                        mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    progressDialog!!.dismiss()

                                    val intent =
                                        Intent(this@SigninActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val message = task.exception!!.toString()
                                    Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG)
                                        .show()
                                    FirebaseAuth.getInstance().signOut()
                                    progressDialog!!.dismiss()
                                }
                            }
                    }
                }
            }
        }

        /*
                private fun loginUser() {

                    val email = findViewById<EditText>(R.id.email_login).text.toString()
                    val password = findViewById<EditText>(R.id.password_login).text.toString()

                    when{
                        TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is required", Toast.LENGTH_LONG).show()
                        TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is required", Toast.LENGTH_LONG).show()

                        else -> {
                            val progressDialog = ProgressDialog(this@SigninActivity)
                            progressDialog.setTitle("Login")
                            progressDialog.setMessage("Please wait, this may take a while...")
                            progressDialog.setCanceledOnTouchOutside(false)
                            progressDialog.show()

                            val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{task ->

                                if (task.isSuccessful){
                                    progressDialog.dismiss()

                                    val intent = Intent(this@SigninActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                } else{
                                    val message = task.exception!!.toString()
                                    Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                                    FirebaseAuth.getInstance().signOut()
                                    progressDialog.dismiss()
                                }

                            }
                        }
                    }

                }

         */


        override fun onResume() {
            super.onResume()

            if (FirebaseAuth.getInstance().currentUser != null) {

                val intent = Intent(this@SigninActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

            /*
override fun onStart() {
    super.onStart()

    if (FirebaseAuth.getInstance().currentUser != null) {
        // Usando o WorkManager para uma operação em segundo plano
        val loginWorkRequest = OneTimeWorkRequestBuilder<LoginWorker>().build()
        WorkManager.getInstance(this).enqueue(loginWorkRequest)
    }
}

         */

        }

        override fun onStop() {
            super.onStop()
            progressDialog!!.dismiss()
        }

        override fun onPause() {
            super.onPause()

            // Oculta o ProgressDialog
            progressDialog?.hide()
        }


        private fun navigateToPhoneActivity() {
            val intent = Intent(this, PhoneActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
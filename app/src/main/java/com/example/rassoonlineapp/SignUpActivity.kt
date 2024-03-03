package com.example.rassoonlineapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        findViewById<Button>(R.id.signin_link_btn).setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }

        findViewById<Button>(R.id.signup_btn).setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val fullName = findViewById<EditText>(R.id.fullname_signup).text.toString()
        val userName = findViewById<EditText>(R.id.username_signup).text.toString()
        val phoneNumber = findViewById<EditText>(R.id.phone_number).text.toString()
        val birthDay = findViewById<EditText>(R.id.birth_signup).text.toString()
        val email = findViewById<EditText>(R.id.email_signup).text.toString()
        val password = findViewById<EditText>(R.id.password_signup).text.toString()

        when{
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "full name is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "User Name is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(phoneNumber) -> Toast.makeText(this, "Phone Number is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(birthDay) -> Toast.makeText(this, "Birthday is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is required", Toast.LENGTH_LONG).show()

            else ->{
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("signUp")
                progressDialog.setMessage("Please wait, this may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {task ->
                            if (task.isSuccessful){

                                saveUserInfo(fullName, userName, phoneNumber,birthDay,email,progressDialog)
                            }
                            else
                            {
                                val message = task.exception!!.toString()
                                 Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                                mAuth.signOut()
                                progressDialog.dismiss()
                            }

                        }
            }
        }

        // Use fullName as needed
    }

    private fun saveUserInfo(fullName: String, userName: String, phoneNumber: String, birthDay: String, email: String, progressDialog: ProgressDialog ) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName.uppercase()
        userMap["username"] = userName.lowercase()
        userMap["phonenumber"] = phoneNumber
        userMap["birthday"] = birthDay
        userMap["email"] = email
        userMap["description"] = currentUserID
        userMap["bio"] = "hello world"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/rasso-online.appspot.com/o/Default%20images%2Fprofile.png?alt=media&token=e2ce3d6b-7364-467d-ba18-9624cc671a34"
        userRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener{task ->
                if (task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this,"A conta foi criada com sucesso", Toast.LENGTH_LONG).show()

                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else{
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }

    }
}

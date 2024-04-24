package com.example.rassoonlineapp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.rassoonlineapp.Model.User
import com.example.rassoonlineapp.WorkManager.UploadImageWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    private val cropActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let {
                    imageUri = it
                    findViewById<CircleImageView>(R.id.profile_image_view_profile_frag).setImageURI(imageUri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Picture")

        findViewById<AppCompatButton>(R.id.delete_account_btn).setOnClickListener {
            showDeleteConfirmationDialog()
        }

        findViewById<ImageView>(R.id.close_profile_btn).setOnClickListener {
            onBackPressed()
        }

        findViewById<TextView>(R.id.change_image_text_btn).setOnClickListener {
            checker = "clicked"
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            cropActivityResultLauncher.launch(intent)
        }

        findViewById<ImageView>(R.id.save_infor_profile_btn).setOnClickListener {
            if (checker == "clicked") {
                uploadImageUsingWorkManager()
            } else {
                updateUserInfoOnly()
            }
        }

        userInfo()
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmação")
            .setMessage("Tem certeza de que deseja excluir sua conta? Esta ação é irreversível.")
            .setPositiveButton("Sim") { dialog, which ->
                deleteAccount()
            }
            .setNegativeButton("Não") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Deletar Conta")
        progressDialog.setMessage("Aguarde, estamos deletando sua conta...")
        progressDialog.show()

        user?.delete()?.addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isSuccessful) {
                deleteUserFromDatabase()
            } else {
                Toast.makeText(
                    this@AccountSettingsActivity,
                    "Falha ao deletar conta. Tente novamente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun deleteUserFromDatabase() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        usersRef.removeValue().addOnCompleteListener { removeTask ->
            if (removeTask.isSuccessful) {
                Toast.makeText(
                    this@AccountSettingsActivity,
                    "Sua conta foi deletada com sucesso",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(this@AccountSettingsActivity, SigninActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this@AccountSettingsActivity,
                    "Falha ao deletar conta. Tente novamente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateUserInfoOnly() {
        val fullname = findViewById<EditText>(R.id.full_name_profile_frag).text.toString()
        val username = findViewById<EditText>(R.id.username_profile_frag).text.toString()
        val description = findViewById<EditText>(R.id.textView_profile_data).text.toString()
        val especialidade = findViewById<EditText>(R.id.especialidade).text.toString()
        val bio = findViewById<EditText>(R.id.bio_profile_frag).text.toString()

        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        val userMap = HashMap<String, Any>()
        userMap["fullname"] = fullname
        userMap["username"] = username
        userMap["description"] = description
        userMap["especialidade"] = especialidade
        userMap["bio"] = bio

        usersRef.updateChildren(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    this@AccountSettingsActivity,
                    "Account Information has been updated successfully",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@AccountSettingsActivity,
                    "Failed to update account information",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
            .child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    val profileImageView = findViewById<CircleImageView>(R.id.profile_image_view_profile_frag)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profileImageView)

                    findViewById<EditText>(R.id.username_profile_frag).setText(user.getUsername())
                    findViewById<EditText>(R.id.full_name_profile_frag).setText(user.getFullname())
                    findViewById<EditText>(R.id.bio_profile_frag).setText(user.getBio())
                    findViewById<EditText>(R.id.textView_profile_data).setText(user.getDescription())
                    findViewById<EditText>(R.id.especialidade).setText(user.getEspecialidade())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun uploadImageUsingWorkManager() {
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_LONG).show()
            return
        }

        val inputData = workDataOf(
            "imageUri" to imageUri.toString(),
            "userId" to firebaseUser.uid
        )

        val uploadImageWorkRequest = OneTimeWorkRequestBuilder<UploadImageWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this).enqueue(uploadImageWorkRequest)

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Account Settings")
        progressDialog.setMessage("Please wait, We are updating your profile...")
        progressDialog.show()
    }
}

package com.example.rassoonlineapp

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.rassoonlineapp.Model.User
import com.google.android.gms.tasks.OnCompleteListener
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
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    private val cropActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
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
            // Cria um AlertDialog.Builder
            val builder = AlertDialog.Builder(this)

            // Define o título e a mensagem do diálogo
            builder.setTitle("Confirmação")
                .setMessage("Tem certeza de que deseja excluir sua conta? Esta ação é irreversível.")

            // Adiciona botões ao diálogo
            builder.setPositiveButton("Sim") { dialog, which ->
                // Usuário clicou em "Sim", proceda com a exclusão da conta
                val user = FirebaseAuth.getInstance().currentUser
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Deletar Conta")
                progressDialog.setMessage("Aguarde, estamos deletando sua conta...")
                progressDialog.show()

                // Remover usuário do Firebase Authentication
                user?.delete()?.addOnCompleteListener { task ->
                    progressDialog.dismiss()
                    if (task.isSuccessful) {
                        // Remover nó do usuário do Realtime Database
                        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
                        usersRef.removeValue().addOnCompleteListener { removeTask ->
                            if (removeTask.isSuccessful) {
                                // Conta e dados do usuário deletados com sucesso
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
                                // Falha ao deletar dados do Realtime Database
                                Toast.makeText(
                                    this@AccountSettingsActivity,
                                    "Falha ao deletar conta. Tente novamente.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        // Falha ao deletar conta do Firebase Authentication
                        Toast.makeText(
                            this@AccountSettingsActivity,
                            "Falha ao deletar conta. Tente novamente.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }


            builder.setNegativeButton("Não") { dialog, which ->
                // Usuário clicou em "Não", fecha o diálogo
                dialog.dismiss()
            }

            // Cria e exibe o AlertDialog
            val alertDialog = builder.create()
            alertDialog.show()
        }


        findViewById<ImageView>(R.id.close_profile_btn).setOnClickListener {
            // Use o FragmentManager para voltar ao ProfileFragment
            val fragmentManager = supportFragmentManager

            // Verifique se há algum fragmento na pilha de fragmentos
            if (fragmentManager.backStackEntryCount > 0) {
                // Se houver fragmentos na pilha, popBackStack() remove o fragmento atual e volta ao anterior
                fragmentManager.popBackStack()
            } else {
                // Se não houver fragmentos na pilha, simplesmente feche a Activity atual
                finish()
            }
        }

        findViewById<TextView>(R.id.change_image_text_btn).setOnClickListener {
            checker = "clicked"
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            cropActivityResultLauncher.launch(intent)
        }

        findViewById<ImageView>(R.id.save_infor_profile_btn).setOnClickListener {
            if (checker == "clicked") {
                UploadImageAndUpdateInfo()
            } else {
                updateUserInfoOnly()
            }
        }
        userInfo()
    }


    private fun updateUserInfoOnly() {
        val fullname = findViewById<EditText>(R.id.full_name_profile_frag).text.toString().uppercase()
        val username = findViewById<EditText>(R.id.username_profile_frag).text.toString().lowercase()
        val description = findViewById<EditText>(R.id.textView_profile_data).text.toString()
        val especialidade = findViewById<EditText>(R.id.especialidade).text.toString()
        val bio = findViewById<EditText>(R.id.bio_profile_frag).text.toString().lowercase()

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

                // Atualize os campos de texto após a conclusão bem-sucedida
                findViewById<EditText>(R.id.full_name_profile_frag).setText(fullname)
                findViewById<EditText>(R.id.username_profile_frag).setText(username)
                findViewById<EditText>(R.id.textView_profile_data).setText(description)
                findViewById<EditText>(R.id.bio_profile_frag).setText(bio)

                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
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

                    findViewById<EditText>(R.id.username_profile_frag).setText(user!!.getUsername())
                    findViewById<EditText>(R.id.full_name_profile_frag).setText(user!!.getFullname())
                    findViewById<EditText>(R.id.bio_profile_frag).setText(user!!.getBio())
                    findViewById<EditText>(R.id.textView_profile_data).setText(user!!.getFullname())
                    findViewById<EditText>(R.id.especialidade).setText(user!!.getEspecialidade())

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun UploadImageAndUpdateInfo() {

        when {
            imageUri == null -> {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(findViewById<EditText>(R.id.full_name_profile_frag).text.toString()) -> {
                Toast.makeText(this, "Please write full name first.", Toast.LENGTH_LONG).show()
            }
            findViewById<EditText>(R.id.username_profile_frag).text.toString() == "" -> {
                Toast.makeText(this, "Please write a user name first.", Toast.LENGTH_LONG).show()
            }
            findViewById<EditText>(R.id.textView_profile_data).text.toString() == "" -> {
                Toast.makeText(this, "Please write a description first.", Toast.LENGTH_LONG).show()
            }
            findViewById<EditText>(R.id.bio_profile_frag).text.toString() == "" -> {
                Toast.makeText(this, "Please write a bio first.", Toast.LENGTH_LONG).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, We are updating your profile...")
                progressDialog.show()

                val fileRef = storageProfilePicRef?.child("${firebaseUser.uid}.jpg")

                val uploadTask = fileRef?.putFile(imageUri!!)

                uploadTask?.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@continueWithTask fileRef.downloadUrl
                }?.addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val usersRef =
                            FirebaseDatabase.getInstance().reference.child("Users")
                                .child(firebaseUser.uid)

                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] =
                            findViewById<EditText>(R.id.full_name_profile_frag).text.toString().uppercase()
                        userMap["username"] =
                            findViewById<EditText>(R.id.username_profile_frag).text.toString().lowercase()
                        userMap["description"] =
                            findViewById<EditText>(R.id.textView_profile_data).text.toString().uppercase()
                        userMap["bio"] =
                            findViewById<EditText>(R.id.bio_profile_frag).text.toString().lowercase()
                        userMap["image"] = myUrl

                        usersRef.updateChildren(userMap)
                            .addOnSuccessListener {
                                progressDialog.dismiss()
                                Toast.makeText(
                                    this,
                                    "Account Information has been updated successfully",
                                    Toast.LENGTH_LONG
                                ).show()

                                val intent =
                                    Intent(this@AccountSettingsActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                progressDialog.dismiss()
                                Toast.makeText(
                                    this,
                                    "Failed to update account information",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }


}
